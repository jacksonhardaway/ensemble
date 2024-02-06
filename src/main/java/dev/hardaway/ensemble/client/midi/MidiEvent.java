package dev.hardaway.ensemble.client.midi;

import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import net.neoforged.bus.api.Event;

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
        return this.status;
    }

    public InstrumentNote getNote() {
        return this.note;
    }

    public int getOctave() {
        return this.octave;
    }

    public int getVelocity() {
        return this.velocity;
    }

    public enum Status {
        ON, OFF
    }
}
