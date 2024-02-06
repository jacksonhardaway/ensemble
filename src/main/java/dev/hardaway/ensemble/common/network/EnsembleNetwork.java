package dev.hardaway.ensemble.common.network;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.client.ClientConductor;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import dev.hardaway.ensemble.common.network.protocol.ClientboundNotePacket;
import dev.hardaway.ensemble.common.network.protocol.ServerboundNotePacket;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class EnsembleNetwork {

    public static void register(RegisterPayloadHandlerEvent event) {
        IPayloadRegistrar registrar = event.registrar(Ensemble.MOD_ID);
        registrar.play(ServerboundNotePacket.ID, ServerboundNotePacket::read, handler -> handler.server((payload, ctx) -> {
            if (ctx.player().isEmpty()) {
                return;
            }

            ServerPlayer sender = (ServerPlayer) ctx.player().get();
            Registry<InstrumentDefinition> registry = sender.level().registryAccess().registryOrThrow(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY);
            if (registry.byId(payload.instrument()) == null) {
                ctx.packetHandler().disconnect(Component.literal("Invalid Instrument")); // TODO translate
                return;
            }

            PacketDistributor.TargetPoint target = new PacketDistributor.TargetPoint(sender, sender.getX(), sender.getY(), sender.getZ(), 24, sender.level().dimension());
            PacketDistributor.NEAR.with(target).send(new ClientboundNotePacket(sender.getId(), payload.status(), payload.note(), payload.instrument(), payload.octave(), payload.velocity()));
        }));registrar.play(ClientboundNotePacket.ID, ClientboundNotePacket::read, handler->handler.client(ClientConductor.INSTANCE::handleNote));
    }
}
