package dev.hardaway.ensemble;

import dev.hardaway.ensemble.client.ClientConductor;
import dev.hardaway.ensemble.client.key.EnsembleKeyMappings;
import dev.hardaway.ensemble.common.network.EnsembleNetwork;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import dev.hardaway.ensemble.common.registry.EnsembleItems;
import dev.hardaway.ensemble.common.registry.EnsembleSynthesizers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Ensemble.MOD_ID)
public class Ensemble {

    public static final String MOD_ID = "ensemble";

    public Ensemble(IEventBus bus) {
        bus.addListener(EnsembleNetwork::register);
        bus.addListener(EnsembleInstruments::register);
        bus.addListener(EnsembleKeyMappings::register);

        EnsembleItems.REGISTRY.register(bus);
        EnsembleSynthesizers.REGISTRY.register(bus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientConductor.INSTANCE.register(bus);
        }
    }
}
