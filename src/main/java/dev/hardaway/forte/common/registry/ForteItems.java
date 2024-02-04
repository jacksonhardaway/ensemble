package dev.hardaway.forte.common.registry;

import dev.hardaway.forte.Forte;
import dev.hardaway.forte.common.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForteItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.ITEMS, Forte.MOD_ID);

    public static final RegistryObject<Item> TEST_INSTRUMENT = REGISTRY.register("test_instrument", () -> new InstrumentItem(new Item.Properties()));
}
