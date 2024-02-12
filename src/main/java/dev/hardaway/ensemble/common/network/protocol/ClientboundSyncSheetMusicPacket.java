package dev.hardaway.ensemble.common.network.protocol;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.instrument.sheetmusic.SheetMusic;
import dev.hardaway.ensemble.common.instrument.sheetmusic.SheetMusicManager;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public record ClientboundSyncSheetMusicPacket(
        Map<ResourceLocation, SheetMusic> sheetMusic) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(Ensemble.MOD_ID, "sync_sheet_music");

    public static void handle(ClientboundSyncSheetMusicPacket payload, IPayloadContext ctx) {
        ctx.workHandler().execute(() -> SheetMusicManager.INSTANCE.syncSheetMusic(payload.sheetMusic));
    }

    // TODO: limit client download size via config
    public static ClientboundSyncSheetMusicPacket read(FriendlyByteBuf buf) {
        return new ClientboundSyncSheetMusicPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, valueBuf -> valueBuf.readWithCodec(NbtOps.INSTANCE, SheetMusic.CODEC, NbtAccounter.create(2097152L))));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        this.sheetMusic.forEach((key, value) -> {
            buf.writeResourceLocation(key);
            buf.writeWithCodec(NbtOps.INSTANCE, SheetMusic.CODEC, value);
        });
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
