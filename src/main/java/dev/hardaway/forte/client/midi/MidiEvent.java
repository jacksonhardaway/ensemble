package dev.hardaway.forte.client.midi;

import dev.hardaway.forte.common.instrument.InstrumentNote;
import net.minecraftforge.eventbus.api.Event;

public class MidiEvent extends Event {

    private final MidiEvent.Status status;
    private final InstrumentNote note;
    private final int octave;
    private final int velocity;

    public MidiEvent(MidiEvent.Status status, InstrumentNote note, int octave, int velocity) {
        this.status = status;
        this.note = note;
        this.octave = octave;
        this.velocity = velocity;
    }

    public MidiEvent.Status getStatus() {
        return status;
    }

    public InstrumentNote getNote() {
        return note;
    }

    public int getOctave() {
        return octave;
    }

    public int getVelocity() {
        return velocity;
    }

    public enum Status {
        ON, OFF
    }
}
