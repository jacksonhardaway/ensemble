package dev.hardaway.forte.common.registry;

import dev.hardaway.forte.Forte;
import dev.hardaway.forte.client.instrument.synthesizer.InstrumentSynthesizerType;
import dev.hardaway.forte.client.instrument.synthesizer.SoundEventSynthesizer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ForteSynthesizers {

    public static final DeferredRegister<InstrumentSynthesizerType> REGISTRY = DeferredRegister.create(new ResourceLocation(Forte.MOD_ID, "instrument_synthesizer_type"), Forte.MOD_ID);
    public static final Supplier<IForgeRegistry<InstrumentSynthesizerType>> REGISTRY_SUPPLIER = REGISTRY.makeRegistry(() -> new RegistryBuilder<InstrumentSynthesizerType>().disableSaving().disableSync());

    public static final RegistryObject<InstrumentSynthesizerType> SOUND_EVENT = REGISTRY.register("sound_event", () -> InstrumentSynthesizerType.of(SoundEventSynthesizer.CODEC));
}
