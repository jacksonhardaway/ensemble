package dev.hardaway.ensemble.common.registry;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EnsembleItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.ITEMS, Ensemble.MOD_ID);

    public static final RegistryObject<Item> TEST_INSTRUMENT = REGISTRY.register("test_instrument", () -> new InstrumentItem(new Item.Properties()));
}
