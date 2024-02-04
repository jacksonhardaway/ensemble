package dev.hardaway.forte.common.network;

import dev.hardaway.forte.Forte;
import dev.hardaway.forte.common.network.protocol.FortePacket;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class ForteNetwork {

    private static final String VERSION = "1";
    public static final SimpleChannel PLAY = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Forte.MOD_ID, "play"),
            () -> ForteNetwork.VERSION,
            ForteNetwork.VERSION::equals,
            ForteNetwork.VERSION::equals
    );

    private static int index = 0;

    public static synchronized void register() {
    }

    private static <MSG extends FortePacket> void register(Class<MSG> clazz, Function<FriendlyByteBuf, MSG> decoder, @Nullable NetworkDirection direction) {
        PLAY.registerMessage(index++, clazz, (msg, friendlyByteBuf) -> {
            try {
                msg.writePacketData(friendlyByteBuf);
            } catch (Exception e) {
                throw new EncoderException(e);
            }
        }, decoder, (msg, ctx) -> {
            NetworkEvent.Context context = ctx.get();
            msg.processPacket(context);
            context.setPacketHandled(true);
        }, Optional.ofNullable(direction));
    }
}
