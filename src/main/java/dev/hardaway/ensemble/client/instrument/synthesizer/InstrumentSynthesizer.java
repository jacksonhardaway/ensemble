package dev.hardaway.ensemble.client.instrument.synthesizer;

import com.mojang.serialization.Codec;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import dev.hardaway.ensemble.common.registry.EnsembleSynthesizers;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

public abstract class InstrumentSynthesizer implements Closeable {

    public static final Codec<? extends InstrumentSynthesizer> CODEC = EnsembleSynthesizers.REGISTRY_SUPPLIER.byNameCodec().dispatch(InstrumentSynthesizer::type, InstrumentSynthesizerType::codec);
    private final InstrumentSynthesizerType<?> type;

    public InstrumentSynthesizer(InstrumentSynthesizerType<?> type) {
        this.type = type;
    }

    /**
     * Synthesizes an instrument note and an octave to a Minecraft sound instance.
     *
     * @param entity     The entity the instrument is being played by
     * @param instrument The instrument the note is being played from
     * @param note       The note being played
     * @param octave     The octave of the note
     * @return The synthesized sound instance, or null if no sound could be synthesized
     */
    @OnlyIn(Dist.CLIENT)
    public abstract @Nullable SoundInstance synthesize(@Nullable Entity entity, InstrumentDefinition instrument, InstrumentNote note, int octave, int velocity);

    @Override
    public void close() {
    }

    public InstrumentSynthesizerType<?> type() {
        return this.type;
    }
}
