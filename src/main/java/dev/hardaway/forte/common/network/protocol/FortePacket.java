package dev.hardaway.forte.common.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

@ApiStatus.Internal
public interface FortePacket {

    /**
     * Writes the raw message data to the data stream.
     *
     * @param buf The buffer to write to
     */
    void writePacketData(FriendlyByteBuf buf) throws IOException;

    /**
     * Passes this message into the specified handler to process the message.
     *
     * @param ctx The context of the message
     */
    void processPacket(NetworkEvent.Context ctx);
}