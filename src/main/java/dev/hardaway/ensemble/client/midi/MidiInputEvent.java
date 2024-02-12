package dev.hardaway.ensemble.client.midi;

import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import net.neoforged.bus.api.Event;

public class MidiInputEvent extends Event {

    private final MidiInputEvent.Status status;
    private final InstrumentNote note;
    private final int octave;
    private final int velocity;

    public MidiInputEvent(MidiInputEvent.Status status, InstrumentNote note, int octave, int velocity) {
        this.status = status;
        this.note = note;
        this.octave = octave;
        this.velocity = velocity;
    }

    public MidiInputEvent.Status getStatus() {
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
