package dev.hardaway.forte.client.instrument.synthesizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hardaway.forte.common.instrument.InstrumentDefinition;
import dev.hardaway.forte.common.instrument.InstrumentNote;
import dev.hardaway.forte.client.instrument.InstrumentSoundInstance;
import dev.hardaway.forte.common.registry.ForteSynthesizers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Map;

public class SoundEventSynthesizer extends InstrumentSynthesizer {

    public static final Codec<SoundEventSynthesizer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(InstrumentNote.CODEC, ResourceLocation.CODEC.listOf()).fieldOf("sound_events").forGetter(SoundEventSynthesizer::getSoundEvents)
    ).apply(instance, SoundEventSynthesizer::new));

    private final Map<InstrumentNote, List<ResourceLocation>> soundEvents;

    public SoundEventSynthesizer(Map<InstrumentNote, List<ResourceLocation>> soundEvents) {
        super(ForteSynthesizers.SOUND_EVENT.get());
        this.soundEvents = soundEvents;
    }

    @Override
    public SoundInstance synthesize(LocalPlayer player, InstrumentDefinition instrument, InstrumentNote note, int octave, int velocity) {
        InstrumentDefinition.NoteDictionaryEntry entry = instrument.getNoteMap().get(note);
        if (octave < entry.octaves().min() || octave > entry.octaves().max())
            return null;

        List<ResourceLocation> soundBank = this.soundEvents.get(note);
        if (soundBank == null || soundBank.isEmpty())
            return null;

        ResourceLocation sound = soundBank.get(octave - entry.octaves().min());
        if (sound == null)
            return null;

        Holder<SoundEvent> event = Holder.direct(SoundEvent.createVariableRangeEvent(sound));
        return new InstrumentSoundInstance(event.value(), SoundSource.PLAYERS, velocity / 127.0F, player);
    }

    public Map<InstrumentNote, List<ResourceLocation>> getSoundEvents() {
        return soundEvents;
    }

}
