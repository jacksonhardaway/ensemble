package dev.hardaway.ensemble.common.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import dev.hardaway.ensemble.common.network.protocol.ClientboundSyncInstrumentPacket;
import dev.hardaway.ensemble.common.registry.EnsembleAttachments;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Ensemble.MOD_ID)
public class InstrumentHolderAttachment {

    public static final Codec<InstrumentHolderAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY).optionalFieldOf("id").forGetter(attachment -> {
                ResourceKey<InstrumentDefinition> value = attachment.get();
                return value == null ? Optional.empty() : Optional.of(value);
            })
    ).apply(instance, optional -> new InstrumentHolderAttachment(optional.orElse(null))));
    private @Nullable ResourceKey<InstrumentDefinition> instrument;
    private @Nullable InstrumentDefinition cachedDefinition;
    private boolean isDirty;

    public InstrumentHolderAttachment(@Nullable ResourceKey<InstrumentDefinition> instrument) {
        this.instrument = instrument;
    }

    @SubscribeEvent
    public static void onEvent(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Optional<ResourceKey<InstrumentDefinition>> instrument = InstrumentHolderAttachment.getInstrumentKey(player);
            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player).send(new ClientboundSyncInstrumentPacket(player.getId(), instrument.orElse(null)));
        }
    }

    @SubscribeEvent
    public static void onEvent(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer player) {
            Optional<ResourceKey<InstrumentDefinition>> instrument = InstrumentHolderAttachment.getInstrumentKey(player);
            PacketDistributor.PLAYER.with((ServerPlayer) event.getEntity()).send(new ClientboundSyncInstrumentPacket(player.getId(), instrument.orElse(null)));
        }
    }

    public static boolean hasInstrument(Player player) {
        InstrumentHolderAttachment holder = player.getData(EnsembleAttachments.INSTRUMENT_HOLDER);
        return holder.instrument != null;
    }

    public static Optional<InstrumentDefinition> getInstrument(Player player) {
        InstrumentHolderAttachment holder = player.getData(EnsembleAttachments.INSTRUMENT_HOLDER);
        if (!holder.isDirty) {
            return Optional.ofNullable(holder.cachedDefinition);
        }

        if (holder.instrument == null) {
            return Optional.ofNullable(holder.cache(null));
        }

        return Optional.ofNullable(
                holder.cache(player.level()
                        .registryAccess()
                        .registryOrThrow(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY)
                        .get(holder.instrument))
        );
    }

    public static Optional<ResourceKey<InstrumentDefinition>> getInstrumentKey(Player player) {
        InstrumentHolderAttachment holder = player.getData(EnsembleAttachments.INSTRUMENT_HOLDER);
        return Optional.ofNullable(holder.get());
    }

    public static void setInstrument(Player player, ResourceKey<InstrumentDefinition> instrument) {
        InstrumentHolderAttachment holder = player.getData(EnsembleAttachments.INSTRUMENT_HOLDER);
        holder.set(instrument);

        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player).send(new ClientboundSyncInstrumentPacket(player.getId(), instrument));
    }

    @Nullable
    public ResourceKey<InstrumentDefinition> get() {
        return instrument;
    }

    public void set(@Nullable ResourceKey<InstrumentDefinition> instrument) {
        this.instrument = instrument;
        this.isDirty = true;
    }

    private InstrumentDefinition cache(InstrumentDefinition instrument) {
        if (!this.isDirty) {
            System.out.println("caching without any changes!");
        }

        this.cachedDefinition = instrument;
        return instrument;
    }
}
