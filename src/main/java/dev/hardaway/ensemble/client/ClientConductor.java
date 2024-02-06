package dev.hardaway.ensemble.client;

import com.mojang.datafixers.util.Pair;
import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.client.instrument.InstrumentSoundInstance;
import dev.hardaway.ensemble.client.instrument.synthesizer.InstrumentSynthesizer;
import dev.hardaway.ensemble.client.instrument.synthesizer.InstrumentSynthesizerManager;
import dev.hardaway.ensemble.client.midi.MidiEvent;
import dev.hardaway.ensemble.client.midi.MidiInterpreter;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import dev.hardaway.ensemble.common.network.protocol.ClientboundNotePacket;
import dev.hardaway.ensemble.common.network.protocol.ServerboundNotePacket;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientConductor {

    public static final ClientConductor INSTANCE = new ClientConductor();
    private static final Map<Pair<InstrumentNote, Integer>, SoundInstance> PLAYING_NOTES = new ConcurrentHashMap<>();
    private final MidiInterpreter midiInterpreter;
    private final InstrumentSynthesizerManager synthesizerManager;

    private ClientConductor() {
        this.midiInterpreter = new MidiInterpreter();
        this.synthesizerManager = new InstrumentSynthesizerManager();
    }

    public void register(IEventBus bus) {
        bus.addListener(this::registerReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::receiveMidi);
    }

    // Enable "recording" mode which consumes the note keybinds
    public void startRecording() {
    }

    // Disable "recording" mode
    public void stopRecording() {
    }

    public void playNote(@Nullable Entity entity, InstrumentDefinition instrument, InstrumentNote note, int octave, int velocity) {
        InstrumentSynthesizer synth = this.synthesizerManager.getSynthesizer(instrument.getSynthesizer());
        if (synth == null) {
            return;
        }

        SoundInstance instance = synth.synthesize(entity, instrument, note, octave, velocity);
        if (instance != null) {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> client.getSoundManager().play(instance));
            PLAYING_NOTES.put(Pair.of(note, octave), instance);
        }
    }

    public void stopNote(@Nullable Entity entity, InstrumentDefinition instrument, InstrumentNote note, int octave) {
        PLAYING_NOTES.computeIfPresent(Pair.of(note, octave), (instrumentNoteIntegerPair, soundInstance) -> {
            if (soundInstance instanceof InstrumentSoundInstance instrumentSound) {
                Minecraft.getInstance().execute(instrumentSound::noteOff);
            }
            return null;
        });
    }

    private void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(this.synthesizerManager);
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> this.midiInterpreter.reload());
    }

    private void receiveMidi(MidiEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        Registry<InstrumentDefinition> registry = player.connection.registryAccess().registryOrThrow(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY);
        InstrumentDefinition instrument = registry.get(new ResourceLocation(Ensemble.MOD_ID, "piano")); // TODO: unhardcode piano
        if (instrument == null) {
            return;
        }

        // TODO don't send packets all the time lol
        sendNote(event.getStatus(), instrument, event.getNote(), event.getOctave(), event.getVelocity());

        if (event.getStatus() == MidiEvent.Status.OFF) {
            this.stopNote(player, instrument, event.getNote(), event.getOctave());
            return;
        }

        this.playNote(player, instrument, event.getNote(), event.getOctave(), event.getVelocity());
    }

    private static void sendNote(MidiEvent.Status status, InstrumentDefinition instrument, InstrumentNote note, int octave, int velocity) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener != null) {
            Registry<InstrumentDefinition> registry = listener.registryAccess().registryOrThrow(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY);
            PacketDistributor.SERVER.noArg().send(new ServerboundNotePacket(status, note, registry.getId(instrument), octave, velocity));
        }
    }

    @ApiStatus.Internal
    public void handleNote(ClientboundNotePacket packet, PlayPayloadContext context) {
        if (context.level().isEmpty()) {
            return;
        }

        Level level = context.level().get();
        Entity entity = level.getEntity(packet.entityId());
        if (entity == null) {
            return;
        }

        Registry<InstrumentDefinition> registry = level.registryAccess().registryOrThrow(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY);
        InstrumentDefinition instrument = registry.byId(packet.instrument());
        if (instrument == null) {
            return;
        }

        MidiEvent.Status status = packet.status();
        InstrumentNote note = packet.note();
        int octave = packet.octave();

        if (status == MidiEvent.Status.ON) {
            this.playNote(entity, instrument, note, octave, packet.velocity());
        } else {
            this.stopNote(entity, instrument, note, octave);
        }
    }

    public MidiInterpreter getMidiInterpreter() {
        return this.midiInterpreter;
    }

    public InstrumentSynthesizerManager getSynthesizerManager() {
        return this.synthesizerManager;
    }
}
