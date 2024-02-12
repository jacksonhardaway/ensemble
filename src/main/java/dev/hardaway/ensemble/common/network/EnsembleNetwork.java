package dev.hardaway.ensemble.common.network;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.network.task.SyncSheetMusicConfigurationTask;
import dev.hardaway.ensemble.common.network.protocol.ClientboundNotePacket;
import dev.hardaway.ensemble.common.network.protocol.ClientboundSyncInstrumentPacket;
import dev.hardaway.ensemble.common.network.protocol.ClientboundSyncSheetMusicPacket;
import dev.hardaway.ensemble.common.network.protocol.UpdateSheetMusicPacket;
import dev.hardaway.ensemble.common.network.protocol.ServerboundNotePacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class EnsembleNetwork {

    public static void register(IEventBus bus) {
        bus.addListener(EnsembleNetwork::registerPackets);
        bus.addListener(EnsembleNetwork::registerConfigurationTasks);
    }

    private static void registerPackets(RegisterPayloadHandlerEvent event) {
        IPayloadRegistrar registrar = event.registrar(Ensemble.MOD_ID);
        registrar.play(ServerboundNotePacket.ID, ServerboundNotePacket::read, handler -> handler.server(ServerboundNotePacket::handle));
        registrar.play(ClientboundNotePacket.ID, ClientboundNotePacket::read, handler -> handler.client(ClientboundNotePacket::handle));
        registrar.play(ClientboundSyncInstrumentPacket.ID, ClientboundSyncInstrumentPacket::read, handler -> handler.client(ClientboundSyncInstrumentPacket::handle));
        registrar.play(UpdateSheetMusicPacket.ID, UpdateSheetMusicPacket::read, handler -> handler.client(UpdateSheetMusicPacket::handle).server(UpdateSheetMusicPacket::handle));
        registrar.play(ClientboundSyncSheetMusicPacket.ID, ClientboundSyncSheetMusicPacket::read, handler -> handler.client(ClientboundSyncSheetMusicPacket::handle));
        registrar.configuration(ClientboundSyncSheetMusicPacket.ID, ClientboundSyncSheetMusicPacket::read, handler -> handler.client(ClientboundSyncSheetMusicPacket::handle));
    }

    private static void registerConfigurationTasks(OnGameConfigurationEvent event) {
        event.register(new SyncSheetMusicConfigurationTask(event.getListener()));
    }
}
