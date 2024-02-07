package dev.hardaway.ensemble.common.item;

import dev.hardaway.ensemble.client.ClientConductor;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InstrumentItem extends Item {

    private final ResourceKey<InstrumentDefinition> instrument;

    public InstrumentItem(ResourceKey<InstrumentDefinition> instrument, Properties properties) {
        super(properties);
        this.instrument = instrument;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pLevel.isClientSide()) {
            ClientConductor conductor = ClientConductor.INSTANCE;
            if (conductor.getRecordingInstrument() == null) {
                conductor.setRecordingInstrument(pLevel.registryAccess().registryOrThrow(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY).get(this.instrument));
            } else {
                conductor.setRecordingInstrument(null);
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    public ResourceKey<InstrumentDefinition> getInstrument() {
        return instrument;
    }
}
