package dev.hardaway.ensemble.common.item;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.attachment.InstrumentHolderAttachment;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import dev.hardaway.ensemble.common.registry.EnsembleAttachments;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

@Mod.EventBusSubscriber(modid = Ensemble.MOD_ID)
public class InstrumentItem extends Item {

    private final ResourceKey<InstrumentDefinition> instrument;

    public InstrumentItem(ResourceKey<InstrumentDefinition> instrument, Properties properties) {
        super(properties);
        this.instrument = instrument;
    }

    @SubscribeEvent
    public static void onEvent(LivingEquipmentChangeEvent event) {
        if (event.getSlot().isArmor())
            return;

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack to = event.getTo();
        ItemStack from = event.getFrom();

        // TODO: make sure this doesn't override an instrument that you're sitting on
        if (to.getItem() instanceof InstrumentItem instrument) {
            InstrumentHolderAttachment.setInstrument(player, instrument.getInstrument());
        } else if (from.getItem() instanceof InstrumentItem) {
            InstrumentHolderAttachment.setInstrument(player, null);
        }
    }

    public ResourceKey<InstrumentDefinition> getInstrument() {
        return instrument;
    }
}
