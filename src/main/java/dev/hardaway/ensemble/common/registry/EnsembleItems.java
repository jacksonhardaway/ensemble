package dev.hardaway.ensemble.common.registry;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.item.InstrumentItem;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EnsembleItems {

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(Ensemble.MOD_ID);

    public static final DeferredItem<InstrumentItem> TEST_INSTRUMENT = registerInstrument("test_instrument", "piano");
    public static final DeferredItem<InstrumentItem> SAXOPHONE = registerInstrument("saxophone", "saxophone");

    private static DeferredItem<InstrumentItem> registerInstrument(String id, String instrument) {
        return REGISTRY.register(id, () -> new InstrumentItem(ResourceKey.create(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY, new ResourceLocation(Ensemble.MOD_ID, instrument)), new Item.Properties()));
    }
}
