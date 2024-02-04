package dev.hardaway.ensemble.common.registry;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.client.instrument.synthesizer.InstrumentSynthesizerType;
import dev.hardaway.ensemble.client.instrument.synthesizer.SoundEventSynthesizer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class EnsembleSynthesizers {

    public static final DeferredRegister<InstrumentSynthesizerType> REGISTRY = DeferredRegister.create(new ResourceLocation(Ensemble.MOD_ID, "instrument_synthesizer_type"), Ensemble.MOD_ID);
    public static final Supplier<IForgeRegistry<InstrumentSynthesizerType>> REGISTRY_SUPPLIER = REGISTRY.makeRegistry(() -> new RegistryBuilder<InstrumentSynthesizerType>().disableSaving().disableSync());

    public static final RegistryObject<InstrumentSynthesizerType> SOUND_EVENT = REGISTRY.register("sound_event", () -> InstrumentSynthesizerType.of(SoundEventSynthesizer.CODEC));
}
