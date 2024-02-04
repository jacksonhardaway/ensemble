package dev.hardaway.ensemble.common.registry;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;

public class EnsembleInstruments {
    public static final ResourceKey<Registry<InstrumentDefinition>> INSTRUMENT_DEFINITION_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(Ensemble.MOD_ID, "instrument_definition"));

    public static void register(IEventBus bus) {
        bus.<DataPackRegistryEvent.NewRegistry>addListener(event ->
                event.dataPackRegistry(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY, InstrumentDefinition.CODEC, InstrumentDefinition.CODEC)
        );
    }
}