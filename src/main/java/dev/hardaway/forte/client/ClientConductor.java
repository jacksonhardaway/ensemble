package dev.hardaway.forte.client;

import com.mojang.datafixers.util.Pair;
import dev.hardaway.forte.Forte;
import dev.hardaway.forte.client.instrument.InstrumentSoundInstance;
import dev.hardaway.forte.client.instrument.synthesizer.InstrumentSynthesizer;
import dev.hardaway.forte.client.instrument.synthesizer.InstrumentSynthesizerManager;
import dev.hardaway.forte.client.midi.MidiEvent;
import dev.hardaway.forte.client.midi.MidiInterpreter;
import dev.hardaway.forte.common.instrument.InstrumentDefinition;
import dev.hardaway.forte.common.instrument.InstrumentNote;
import dev.hardaway.forte.common.network.ForteNetwork;
import dev.hardaway.forte.common.registry.ForteInstruments;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkEvent;
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
        ForteNetwork.NOTES.<NetworkEvent.ServerCustomPayloadEvent>addListener(networkEvent -> {
            NetworkEvent.Context ctx = networkEvent.getSource().get();
            ctx.setPacketHandled(true);

            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection == null) {
                return;
            }

            FriendlyByteBuf buf = networkEvent.getPayload();
            int entityId = buf.readVarInt();

            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }

            Entity entity = level.getEntity(entityId);
            if (entity == null) {
                return;
            }

            int instrumentId = buf.readVarInt();
            Registry<InstrumentDefinition> registry = connection.registryAccess().registryOrThrow(ForteInstruments.INSTRUMENT_DEFINITION_REGISTRY);
            InstrumentDefinition instrument = registry.byId(instrumentId);
            if (instrument == null) {
                return;
            }

            MidiEvent.Status status = buf.readEnum(MidiEvent.Status.class);
            InstrumentNote note = buf.readEnum(InstrumentNote.class);
            int octave = buf.readVarInt();
            int velocity = status == MidiEvent.Status.ON ? buf.readVarInt() : 0;

            if (status == MidiEvent.Status.ON) {
                this.playNote(entity, instrument, note, octave, velocity);
            } else {
                this.stopNote(entity, instrument, note, octave);
            }
        });
    }

    public void register(IEventBus bus) {
        bus.addListener(this::registerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(this::receiveMidi);
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

        Registry<InstrumentDefinition> registry = player.connection.registryAccess().registryOrThrow(ForteInstruments.INSTRUMENT_DEFINITION_REGISTRY);
        InstrumentDefinition instrument = registry.get(new ResourceLocation(Forte.MOD_ID, "piano")); // TODO: unhardcode piano
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
        ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (clientPacketListener == null) {
            return;
        }

        Registry<InstrumentDefinition> registry = clientPacketListener.registryAccess().registryOrThrow(ForteInstruments.INSTRUMENT_DEFINITION_REGISTRY);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(registry.getId(instrument));
        buf.writeEnum(status);
        buf.writeEnum(note);
        buf.writeVarInt(octave);
        if (status == MidiEvent.Status.ON) {
            buf.writeVarInt(velocity);
        }
        clientPacketListener.getConnection().send(new ServerboundCustomPayloadPacket(ForteNetwork.NOTES_CHANNEL, buf));
    }

    public MidiInterpreter getMidiInterpreter() {
        return this.midiInterpreter;
    }

    public InstrumentSynthesizerManager getSynthesizerManager() {
        return this.synthesizerManager;
    }
}
