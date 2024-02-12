package dev.hardaway.ensemble.common.instrument.sheetmusic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hardaway.ensemble.common.network.EnsembleCodecs;

import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;

public record SheetMusic(MidiFileFormat header, Sequence sequence) {

    public static final Codec<SheetMusic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EnsembleCodecs.MIDI_FILE_FORMAT.fieldOf("header").forGetter(SheetMusic::header),
            EnsembleCodecs.SEQUENCE.fieldOf("sequence").forGetter(SheetMusic::sequence)
    ).apply(instance, SheetMusic::new));
}
