package dev.hardaway.forte.common.network;

import dev.hardaway.forte.Forte;
import dev.hardaway.forte.client.midi.MidiEvent;
import dev.hardaway.forte.common.instrument.InstrumentDefinition;
import dev.hardaway.forte.common.instrument.InstrumentNote;
import dev.hardaway.forte.common.network.protocol.FortePacket;
import dev.hardaway.forte.common.registry.ForteInstruments;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.event.EventNetworkChannel;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class ForteNetwork {

    private static final String VERSION = "1";
    public static final ResourceLocation NOTES_CHANNEL = new ResourceLocation(Forte.MOD_ID, "notes");
    public static final SimpleChannel PLAY = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Forte.MOD_ID, "play"),
            () -> ForteNetwork.VERSION,
            ForteNetwork.VERSION::equals,
            ForteNetwork.VERSION::equals);
    public static final EventNetworkChannel NOTES = NetworkRegistry.newEventChannel(
            NOTES_CHANNEL,
            () -> ForteNetwork.VERSION,
            ForteNetwork.VERSION::equals,
            ForteNetwork.VERSION::equals);

    private static int index = 0;

    public static synchronized void register() {
        NOTES.<NetworkEvent.ClientCustomPayloadEvent>addListener(networkEvent -> {
            NetworkEvent.Context ctx = networkEvent.getSource().get();
            ctx.setPacketHandled(true);

            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }

            FriendlyByteBuf buf = networkEvent.getPayload();
            int instrument = buf.readVarInt();

            Registry<InstrumentDefinition> registry = sender.level().registryAccess().registryOrThrow(ForteInstruments.INSTRUMENT_DEFINITION_REGISTRY);
            if (registry.byId(instrument) == null) {
                Component reason = Component.literal("Invalid Instrument"); // TODO translate
                sender.connection.send(new ClientboundDisconnectPacket(reason), PacketSendListener.thenRun(() -> sender.connection.disconnect(reason)));
                return;
            }

            MidiEvent.Status status = buf.readEnum(MidiEvent.Status.class);
            InstrumentNote note = buf.readEnum(InstrumentNote.class);
            int octave = buf.readVarInt();
            int velocity = status == MidiEvent.Status.ON ? buf.readVarInt() : 0;
            int entityId = sender.getId();

            FriendlyByteBuf sendBuf = new FriendlyByteBuf(Unpooled.buffer());
            sendBuf.writeVarInt(entityId);
            sendBuf.writeVarInt(instrument);
            sendBuf.writeEnum(status);
            sendBuf.writeEnum(note);
            sendBuf.writeVarInt(octave);
            if (status == MidiEvent.Status.ON) {
                sendBuf.writeVarInt(velocity);
            }
            
            PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(sender, sender.getX(), sender.getY(), sender.getZ(), 24, sender.level().dimension())).send(new ClientboundCustomPayloadPacket(NOTES_CHANNEL, sendBuf));
        });
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
