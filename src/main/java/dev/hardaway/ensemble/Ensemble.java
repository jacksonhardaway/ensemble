package dev.hardaway.ensemble;

import dev.hardaway.ensemble.client.ClientConductor;
import dev.hardaway.ensemble.common.network.EnsembleNetwork;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import dev.hardaway.ensemble.common.registry.EnsembleItems;
import dev.hardaway.ensemble.common.registry.EnsembleSynthesizers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Ensemble.MOD_ID)
public class Ensemble {

    public static final String MOD_ID = "ensemble";

    public Ensemble() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);

        EnsembleItems.REGISTRY.register(bus);
        EnsembleSynthesizers.REGISTRY.register(bus);
        EnsembleInstruments.register(bus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientConductor.INSTANCE.register(bus));

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        EnsembleNetwork.register();
    }
}
