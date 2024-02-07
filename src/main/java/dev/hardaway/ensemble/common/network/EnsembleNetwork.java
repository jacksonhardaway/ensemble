package dev.hardaway.ensemble.common.network;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.network.protocol.ClientboundNotePacket;
import dev.hardaway.ensemble.common.network.protocol.ServerboundNotePacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class EnsembleNetwork {

    public static void register(RegisterPayloadHandlerEvent event) {
        IPayloadRegistrar registrar = event.registrar(Ensemble.MOD_ID);
        registrar.play(ServerboundNotePacket.ID, ServerboundNotePacket::read, handler -> handler.server(ServerboundNotePacket::handle));
        registrar.play(ClientboundNotePacket.ID, ClientboundNotePacket::read, handler -> handler.client(ClientboundNotePacket::handle));
    }
}
