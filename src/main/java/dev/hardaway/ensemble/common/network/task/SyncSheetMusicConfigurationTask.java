package dev.hardaway.ensemble.common.network.task;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.instrument.sheetmusic.SheetMusicManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.function.Consumer;

public record SyncSheetMusicConfigurationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(new ResourceLocation(Ensemble.MOD_ID, "sync_sheet_music"));

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        SheetMusicManager.INSTANCE.sync(sender);
        this.listener.finishCurrentTask(this.type());
    }

    @Override
    public Type type() {
        return SyncSheetMusicConfigurationTask.TYPE;
    }
}
