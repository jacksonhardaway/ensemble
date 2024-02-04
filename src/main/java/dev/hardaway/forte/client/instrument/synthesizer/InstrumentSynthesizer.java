package dev.hardaway.forte.client.instrument.synthesizer;

import com.mojang.serialization.Codec;
import dev.hardaway.forte.common.instrument.InstrumentDefinition;
import dev.hardaway.forte.common.instrument.InstrumentNote;
import dev.hardaway.forte.common.registry.ForteSynthesizers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.jetbrains.annotations.Nullable;

public abstract class InstrumentSynthesizer {

    public static final Codec<? extends InstrumentSynthesizer> CODEC = ForteSynthesizers.REGISTRY_SUPPLIER.get().getCodec().dispatch(InstrumentSynthesizer::type, InstrumentSynthesizerType::codec);
    private final InstrumentSynthesizerType type;

    public InstrumentSynthesizer(InstrumentSynthesizerType type) {
        this.type = type;
    }

    /**
     * Synthesizes an instrument note and an octave to a Minecraft sound instance.
     * @param player The player the instrument is being played by
     * @param instrument The instrument the note is being played from
     * @param note The note being played
     * @param octave The octave of the note
     * @return The synthesized sound instance, or null if no sound could be synthesized
     */
    @Nullable
    public abstract SoundInstance synthesize(LocalPlayer player, InstrumentDefinition instrument, InstrumentNote note, int octave, int velocity);

    public InstrumentSynthesizerType type() {
        return type;
    }
}
