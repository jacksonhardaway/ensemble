package dev.hardaway.ensemble.common.instrument;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;

public final class InstrumentDefinition {
    public static final Codec<InstrumentDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("synthesizer").forGetter(InstrumentDefinition::getSynthesizer),
                    Codec.BOOL.optionalFieldOf("can_sustain", false).forGetter(InstrumentDefinition::canSustain),
                    NoteDictionaryEntry.CODEC.listOf().fieldOf("note_dictionary").forGetter(def -> def.noteDictionary)
            ).apply(instance, InstrumentDefinition::new));

    private final ResourceLocation synthesizer;
    private final boolean canSustain;
    private final List<NoteDictionaryEntry> noteDictionary;
    private final Map<InstrumentNote, NoteDictionaryEntry> noteMap;
    private final int minOctave;
    private final int maxOctave;

    private InstrumentDefinition(ResourceLocation synthesizer, boolean canSustain, List<NoteDictionaryEntry> noteDictionary) {
        this.synthesizer = synthesizer;
        this.canSustain = canSustain;
        this.noteDictionary = noteDictionary;

        int minOctave = 0;
        int maxOctave = 0;

        ImmutableMap.Builder<InstrumentNote, NoteDictionaryEntry> noteMap = ImmutableMap.builder();
        for (NoteDictionaryEntry entry : noteDictionary) {
            NoteDictionaryEntry.Octaves octaves = entry.octaves();
            if (maxOctave < octaves.max()) {
                maxOctave = octaves.max();
            }
            if (minOctave > octaves.min()) {
                minOctave = octaves.min();
            }

            noteMap.put(entry.note(), entry);
        }
        this.noteMap = noteMap.build();
        this.minOctave = minOctave;
        this.maxOctave = maxOctave;
    }

    public ResourceLocation getSynthesizer() {
        return synthesizer;
    }

    public boolean canSustain() {
        return canSustain;
    }

    public Map<InstrumentNote, NoteDictionaryEntry> getNoteMap() {
        return noteMap;
    }

    public int clampOctave(int octave) {
        return Mth.clamp(octave, this.getMinOctave(), this.getMaxOctave());
    }

    public int getMinOctave() {
        return minOctave;
    }

    public int getMaxOctave() {
        return maxOctave;
    }

    public record NoteDictionaryEntry(InstrumentNote note, Octaves octaves) {
        public static final Codec<NoteDictionaryEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        InstrumentNote.CODEC.fieldOf("note").forGetter(NoteDictionaryEntry::note),
                        Octaves.CODEC.optionalFieldOf("octaves", new Octaves(0, 0)).forGetter(NoteDictionaryEntry::octaves)
                ).apply(instance, NoteDictionaryEntry::new));

        public record Octaves(int min, int max) {
            public static final Codec<NoteDictionaryEntry.Octaves> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.fieldOf("min").forGetter(Octaves::min),
                            Codec.INT.fieldOf("max").forGetter(Octaves::max)
                    ).apply(instance, Octaves::new));
        }
    }

}
