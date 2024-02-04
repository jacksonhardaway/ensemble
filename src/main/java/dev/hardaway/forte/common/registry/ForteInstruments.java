package dev.hardaway.forte.common.registry;

import dev.hardaway.forte.Forte;
import dev.hardaway.forte.common.instrument.InstrumentDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;

public class ForteInstruments {
    public static final ResourceKey<Registry<InstrumentDefinition>> INSTRUMENT_DEFINITION_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(Forte.MOD_ID, "instrument_definition"));

    public static void register(IEventBus bus) {
        bus.<DataPackRegistryEvent.NewRegistry>addListener(event ->
                event.dataPackRegistry(ForteInstruments.INSTRUMENT_DEFINITION_REGISTRY, InstrumentDefinition.CODEC, InstrumentDefinition.CODEC)
        );
    }
}