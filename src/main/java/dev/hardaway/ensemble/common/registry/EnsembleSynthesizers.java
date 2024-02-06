package dev.hardaway.ensemble.common.registry;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.client.instrument.synthesizer.InstrumentSynthesizerType;
import dev.hardaway.ensemble.client.instrument.synthesizer.SoundEventSynthesizer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EnsembleSynthesizers {

    public static final ResourceKey<Registry<InstrumentSynthesizerType>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Ensemble.MOD_ID, "instrument_synthesizer_type"));
    public static final DeferredRegister<InstrumentSynthesizerType> REGISTRY = DeferredRegister.create(REGISTRY_KEY, Ensemble.MOD_ID);
    public static final Registry<InstrumentSynthesizerType> REGISTRY_SUPPLIER = REGISTRY.makeRegistry(builder -> {
    });

    public static final Holder<InstrumentSynthesizerType> SOUND_EVENT = REGISTRY.register("sound_event", () -> InstrumentSynthesizerType.of(SoundEventSynthesizer.CODEC));
}
