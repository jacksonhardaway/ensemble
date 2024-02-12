package dev.hardaway.ensemble.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import dev.hardaway.ensemble.client.instrument.InstrumentSoundInstance;
import dev.hardaway.ensemble.client.instrument.synthesizer.InstrumentSynthesizer;
import dev.hardaway.ensemble.client.instrument.synthesizer.InstrumentSynthesizerManager;
import dev.hardaway.ensemble.client.key.EnsembleKeyMappings;
import dev.hardaway.ensemble.client.key.NoteKeyMapping;
import dev.hardaway.ensemble.client.midi.MidiInputEvent;
import dev.hardaway.ensemble.client.midi.MidiInterpreter;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import dev.hardaway.ensemble.common.network.protocol.ClientboundNotePacket;
import dev.hardaway.ensemble.common.network.protocol.ServerboundNotePacket;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientConductor {

    public static final ClientConductor INSTANCE = new ClientConductor();
    private static final Map<Pair<InstrumentNote, Integer>, SoundInstance> PLAYING_NOTES = new ConcurrentHashMap<>();
    private static final Set<Pair<InstrumentNote, Integer>> CLIENT_NOTES = new HashSet<>();
    private final MidiInterpreter midiInterpreter;
    private final InstrumentSynthesizerManager synthesizerManager;

    private InstrumentDefinition instrument;
    private int octave;

    private ClientConductor() {
        this.midiInterpreter = new MidiInterpreter();
        this.synthesizerManager = new InstrumentSynthesizerManager();
    }

    private static void sendNote(MidiInputEvent.Status status, InstrumentDefinition instrument, InstrumentNote note, int octave, int velocity) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener != null) {
            Registry<InstrumentDefinition> registry = listener.registryAccess().registryOrThrow(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY);
            PacketDistributor.SERVER.noArg().send(new ServerboundNotePacket(status, note, registry.getId(instrument), octave, velocity));
        }
    }

    public void register(IEventBus bus) {
        bus.addListener(this::registerReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::receiveMidi);
        NeoForge.EVENT_BUS.addListener(this::captureNoteKeys);
    }

    public void setInstrument(@Nullable InstrumentDefinition instrument) {
        CLIENT_NOTES.forEach((pair) -> {
            sendNote(MidiInputEvent.Status.OFF, this.instrument, pair.getFirst(), pair.getSecond(), 0);
            this.stopNote(pair.getFirst(), pair.getSecond());
        });
        CLIENT_NOTES.clear();

        this.instrument = instrument;
        if (this.instrument != null) {
            this.octave = instrument.clampOctave((instrument.getMaxOctave() - instrument.getMinOctave() + 1) / 2);
        }
    }

    public void playNote(@Nullable Entity entity, InstrumentDefinition instrument, InstrumentNote note, int octave, int velocity, boolean self) {
        InstrumentSynthesizer synth = this.synthesizerManager.getSynthesizer(instrument.getSynthesizer());
        if (synth == null) {
            return;
        }

        SoundInstance instance = synth.synthesize(entity, instrument, note, octave, velocity);
        if (instance != null) {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> client.getSoundManager().play(instance));
            Pair<InstrumentNote, Integer> notePair = Pair.of(note, octave);
            PLAYING_NOTES.put(notePair, instance);
            if (self) {
                CLIENT_NOTES.add(notePair);
            }
        }
    }

    public void stopNote(InstrumentNote note, int octave) {
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

    private void captureNoteKeys(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (this.instrument == null) {
            return;
        }

        InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
        if (event.getAction() == InputConstants.PRESS) {
            if (EnsembleKeyMappings.TRANSPOSE_OCTAVE_UP.get().isActiveAndMatches(key)) {
                this.octave = this.instrument.clampOctave(this.octave + 1);
                return;
            } else if (EnsembleKeyMappings.TRANSPOSE_OCTAVE_DOWN.get().isActiveAndMatches(key)) {
                this.octave = this.instrument.clampOctave(this.octave - 1);
                return;
            }
        }

        for (NoteKeyMapping keyMapping : EnsembleKeyMappings.getNoteKeySet()) {
            if (!keyMapping.isActiveAndMatches(InputConstants.getKey(event.getKey(), event.getScanCode())))
                continue;

            InstrumentNote note = keyMapping.getNote();
            boolean sharpen = EnsembleKeyMappings.SHARPEN_NOTE.get().isDown() && note.canSharpen();

            switch (event.getAction()) {
                case InputConstants.PRESS -> {
                    keyMapping.setOctave(this.octave);
                    keyMapping.setSharpened(sharpen);
                    if (sharpen) {
                        note = note.sharpen();
                    }

                    sendNote(MidiInputEvent.Status.ON, this.instrument, note, this.octave, 127);
                    this.playNote(player, this.instrument, note, this.octave, 127, true);
                    return;
                }
                case InputConstants.RELEASE -> {
                    final int octave = keyMapping.getOctave();
                    final InstrumentNote releaseNote = keyMapping.isSharpened() ? note.sharpen() : note;

                    sendNote(MidiInputEvent.Status.OFF, this.instrument, releaseNote, octave, 0);
                    this.stopNote(releaseNote, octave);
                    return;
                }
            }
        }
    }

    private void receiveMidi(MidiInputEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (this.instrument == null) {
            return;
        }

        sendNote(event.getStatus(), this.instrument, event.getNote(), event.getOctave(), event.getVelocity());

        switch (event.getStatus()) {
            case ON -> this.playNote(player, this.instrument, event.getNote(), event.getOctave(), event.getVelocity(), true);
            case OFF -> this.stopNote(event.getNote(), event.getOctave());
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

        MidiInputEvent.Status status = packet.status();
        InstrumentNote note = packet.note();
        int octave = packet.octave();

        if (status == MidiInputEvent.Status.ON) {
            this.playNote(entity, instrument, note, octave, packet.velocity(), false);
        } else {
            this.stopNote(note, octave);
        }
    }
}
