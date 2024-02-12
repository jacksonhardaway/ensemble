package dev.hardaway.ensemble;

import dev.hardaway.ensemble.client.ClientConductor;
import dev.hardaway.ensemble.client.key.EnsembleKeyMappings;
import dev.hardaway.ensemble.common.instrument.sheetmusic.SheetMusicManager;
import dev.hardaway.ensemble.common.network.EnsembleNetwork;
import dev.hardaway.ensemble.common.registry.EnsembleAttachments;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import dev.hardaway.ensemble.common.registry.EnsembleItems;
import dev.hardaway.ensemble.common.registry.EnsembleSynthesizers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Ensemble.MOD_ID)
public class Ensemble {

    public static final String MOD_ID = "ensemble";

    public Ensemble(IEventBus bus) {
        bus.addListener(EnsembleInstruments::register);
        bus.addListener(EnsembleKeyMappings::register);

        NeoForge.EVENT_BUS.addListener(SheetMusicManager::register);

        EnsembleNetwork.register(bus);

        EnsembleItems.REGISTRY.register(bus);
        EnsembleSynthesizers.REGISTRY.register(bus);
        EnsembleAttachments.REGISTRY.register(bus);

        if (FMLEnvironment.dist.isClient()) {
            ClientConductor.INSTANCE.register(bus);
        }
    }
}
