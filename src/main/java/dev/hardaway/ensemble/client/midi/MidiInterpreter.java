package dev.hardaway.ensemble.client.midi;

import com.mojang.logging.LogUtils;
import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.HashMap;
import java.util.Map;

public final class MidiInterpreter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<MidiDevice, Transmitter> devices = new HashMap<>();

    public MidiInterpreter() {
    }

    public void reload() {
        this.free();
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            MidiDevice device;
            try {
                device = MidiSystem.getMidiDevice(info);
                device.open();
            } catch (MidiUnavailableException e) {
                LOGGER.error("Failed to get midi device: " + info.getName(), e);
                continue;
            }

            Transmitter transmitter;
            try {
                transmitter = device.getTransmitter();
            } catch (MidiUnavailableException e) {
                // We don't care if this throws an exception. If there is no transmitter then the MIDI device cannot send data.
                LOGGER.debug("No transmitter found for " + info.getName(), e);
                device.close();
                continue;
            }
            transmitter.setReceiver(new Handler());

            LOGGER.debug("Found midi device: {} \"{}\" by {} ({})", info.getName(), info.getDescription(), info.getVendor(), info.getVersion());
            this.devices.put(device, transmitter);
        }
        LOGGER.info("Loaded {} midi devices", this.devices.size());
    }

    public void free() {
        this.devices.forEach((midiDevice, transmitter) -> midiDevice.close());
        this.devices.clear();
    }

    public static class Handler implements Receiver {
        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (!(message instanceof ShortMessage msg))
                return;

            int command = msg.getCommand();
            if (command != ShortMessage.NOTE_ON && command != ShortMessage.NOTE_OFF)
                return;

            int noteData = msg.getData1();
            InstrumentNote note = InstrumentNote.from(noteData % 12);
            if (note == null)
                return;

            int octave = (noteData / 12) - 1;
            int velocity = msg.getData2();

            LOGGER.debug("received midi event: {} oct {} vel {}", note.getSerializedName(), octave, velocity);
            Minecraft.getInstance().execute(() -> MinecraftForge.EVENT_BUS.post(new MidiEvent(command == ShortMessage.NOTE_OFF || velocity <= 0 ? MidiEvent.Status.OFF : MidiEvent.Status.ON, note, octave, velocity)));
        }

        @Override
        public void close() {
        }
    }
}
