package dev.hardaway.ensemble.common.network.protocol;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.instrument.sheetmusic.SheetMusic;
import dev.hardaway.ensemble.common.instrument.sheetmusic.SheetMusicManager;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.Nullable;

public record UpdateSheetMusicPacket(ResourceLocation location,
                                     @Nullable SheetMusic sheetMusic) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(Ensemble.MOD_ID, "update_sheet_music");

    // TODO: check for operator status on the server
    public static void handle(UpdateSheetMusicPacket payload, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> {
            ResourceLocation location = payload.location;
            SheetMusic music = payload.sheetMusic;
            if (music != null) {
                SheetMusicManager.INSTANCE.add(location, music);
            } else {
                SheetMusicManager.INSTANCE.remove(location);
            }
        });
    }

    // TODO: limit size via config
    public static UpdateSheetMusicPacket read(FriendlyByteBuf buf) {
        return new UpdateSheetMusicPacket(buf.readResourceLocation(), buf.readNullable(reader -> reader.readWithCodec(NbtOps.INSTANCE, SheetMusic.CODEC, NbtAccounter.create(2097152L))));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.location);
        buf.writeNullable(this.sheetMusic, (writer, value) -> buf.writeWithCodec(NbtOps.INSTANCE, SheetMusic.CODEC, value));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
