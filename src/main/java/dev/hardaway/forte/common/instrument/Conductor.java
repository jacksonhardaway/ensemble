package dev.hardaway.forte.common.instrument;

public interface Conductor {
    
    void startRecording();

    void stopRecording();

    void playNote(InstrumentDefinition instrument, InstrumentNote note, int octave, int velocity);
}
