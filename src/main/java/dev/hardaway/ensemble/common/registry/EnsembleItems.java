package dev.hardaway.ensemble.common.registry;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.item.InstrumentItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EnsembleItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.createItems(Ensemble.MOD_ID);

    public static final Holder<Item> TEST_INSTRUMENT = REGISTRY.register("test_instrument", () -> new InstrumentItem(new Item.Properties()));
}
