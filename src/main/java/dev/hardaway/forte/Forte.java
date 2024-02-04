package dev.hardaway.forte;

import dev.hardaway.forte.client.ClientConductor;
import dev.hardaway.forte.common.network.ForteNetwork;
import dev.hardaway.forte.common.registry.ForteInstruments;
import dev.hardaway.forte.common.registry.ForteItems;
import dev.hardaway.forte.common.registry.ForteSynthesizers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Forte.MOD_ID)
public class Forte {

    public static final String MOD_ID = "forte";

    public Forte() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);

        ForteItems.REGISTRY.register(bus);
        ForteSynthesizers.REGISTRY.register(bus);
        ForteInstruments.register(bus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientConductor.INSTANCE.register(bus));

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ForteNetwork.register();
    }
}
