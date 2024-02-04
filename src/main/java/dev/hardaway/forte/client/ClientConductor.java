package dev.hardaway.forte.client;

import com.mojang.datafixers.util.Pair;
import dev.hardaway.forte.Forte;
import dev.hardaway.forte.common.instrument.InstrumentDefinition;
import dev.hardaway.forte.common.instrument.InstrumentNote;
import dev.hardaway.forte.client.instrument.synthesizer.InstrumentSynthesizer;
import dev.hardaway.forte.client.instrument.InstrumentSoundInstance;
import dev.hardaway.forte.client.instrument.synthesizer.InstrumentSynthesizerManager;
import dev.hardaway.forte.client.midi.MidiEvent;
import dev.hardaway.forte.client.midi.MidiInterpreter;
import dev.hardaway.forte.common.instrument.InstrumentManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;

public final class ClientConductor {

    public static final ClientConductor INSTANCE = new ClientConductor();
    private static final Map<Pair<InstrumentNote, Integer>, SoundInstance> PLAYING_NOTES = new HashMap<>();
    private final MidiInterpreter midiInterpreter;
    private final InstrumentSynthesizerManager synthesizerManager;

    private ClientConductor() {
        this.midiInterpreter = new MidiInterpreter();
        this.synthesizerManager = new InstrumentSynthesizerManager();
    }

    public void register(IEventBus bus) {
        bus.addListener(this::setup);
        bus.addListener(this::registerReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(this::receiveMidi);
    }

    // Enable "recording" mode which consumes the note keybinds
    public void startRecording() {}

    // Disable "recording" mode
    public void stopRecording() {}

    public void playNote(InstrumentDefinition instrument, InstrumentNote note, int octave, int volume) {}

    // Queue notes for network use?
    public void queueForNetwork() {}

    private void setup(FMLClientSetupEvent event) {
        this.midiInterpreter.reload();
    }

    private void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(this.synthesizerManager);
    }

    private void receiveMidi(MidiEvent event) {
        SoundManager sm = Minecraft.getInstance().getSoundManager();
        if (event.getStatus() == MidiEvent.Status.OFF) {
            PLAYING_NOTES.computeIfPresent(Pair.of(event.getNote(), event.getOctave()), (instrumentNoteIntegerPair, soundInstance) -> {
                if (soundInstance instanceof InstrumentSoundInstance instrumentSound) {
                    instrumentSound.noteOff();
                }
                return null;
            });

            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        InstrumentDefinition instrument = InstrumentManager.INSTANCE.getInstrument(new ResourceLocation(Forte.MOD_ID, "piano"));
        if (instrument == null)
            return;

        InstrumentSynthesizer synth = this.synthesizerManager.getSynthesizer(instrument.getSynthesizer());
        if (synth == null)
            return;

        SoundInstance instance = synth.synthesize(Minecraft.getInstance().player, instrument, event.getNote(), event.getOctave(), event.getVelocity());
        if (instance != null) {
            sm.play(instance);
            PLAYING_NOTES.put(Pair.of(event.getNote(), event.getOctave()), instance);
        }
    }

    public MidiInterpreter getMidiInterpreter() {
        return midiInterpreter;
    }

    public InstrumentSynthesizerManager getSynthesizerManager() {
        return synthesizerManager;
    }
}
