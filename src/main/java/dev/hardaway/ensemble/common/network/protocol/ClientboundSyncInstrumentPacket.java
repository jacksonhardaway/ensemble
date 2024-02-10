package dev.hardaway.ensemble.common.network.protocol;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.client.ClientConductor;
import dev.hardaway.ensemble.common.attachment.InstrumentHolderAttachment;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import dev.hardaway.ensemble.common.registry.EnsembleAttachments;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record ClientboundSyncInstrumentPacket(int entityId,
                                              ResourceKey<InstrumentDefinition> instrument) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(Ensemble.MOD_ID, "sync_instrument");

    public static ClientboundSyncInstrumentPacket read(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        ResourceKey<InstrumentDefinition> instrument = buf.readNullable(reader -> reader.readResourceKey(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY));
        return new ClientboundSyncInstrumentPacket(entityId, instrument);
    }

    public static void handle(ClientboundSyncInstrumentPacket payload, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> {
            Level level = ctx.level().orElse(null);
            if (level == null)
                return;

            Entity entity = level.getEntity(payload.entityId);
            if (entity == null)
                return;

            InstrumentHolderAttachment holder = entity.getData(EnsembleAttachments.INSTRUMENT_HOLDER);
            holder.set(payload.instrument);

            if (entity.equals(Minecraft.getInstance().player)) {
                ClientConductor.INSTANCE.setInstrument(InstrumentHolderAttachment.getInstrument(Minecraft.getInstance().player).orElse(null));
            }
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeNullable(this.instrument, FriendlyByteBuf::writeResourceKey);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
