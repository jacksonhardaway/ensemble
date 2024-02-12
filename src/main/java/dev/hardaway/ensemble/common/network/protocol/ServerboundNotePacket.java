package dev.hardaway.ensemble.common.network.protocol;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.client.midi.MidiInputEvent;
import dev.hardaway.ensemble.common.instrument.InstrumentDefinition;
import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import dev.hardaway.ensemble.common.registry.EnsembleInstruments;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record ServerboundNotePacket(MidiInputEvent.Status status,
                                    InstrumentNote note,
                                    int instrument,
                                    int octave,
                                    int velocity) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(Ensemble.MOD_ID, "serverbound_note");
    private static final Component INVALID_INSTRUMENT = Component.translatable("disconnect.ensemble.invalid_instrument");

    public static ServerboundNotePacket read(FriendlyByteBuf buf) {
        MidiInputEvent.Status status = buf.readEnum(MidiInputEvent.Status.class);
        InstrumentNote note = buf.readEnum(InstrumentNote.class);
        int instrument = buf.readVarInt();
        int octave = buf.readVarInt();
        int velocity = status == MidiInputEvent.Status.ON ? buf.readVarInt() : 0;
        return new ServerboundNotePacket(status, note, instrument, octave, velocity);
    }

    public static void handle(ServerboundNotePacket payload, PlayPayloadContext ctx) {
        if (ctx.player().isEmpty()) {
            return;
        }

        ServerPlayer sender = (ServerPlayer) ctx.player().get();
        Registry<InstrumentDefinition> registry = sender.level().registryAccess().registryOrThrow(EnsembleInstruments.INSTRUMENT_DEFINITION_REGISTRY);
        if (registry.byId(payload.instrument()) == null) {
            ctx.packetHandler().disconnect(ServerboundNotePacket.INVALID_INSTRUMENT);
            return;
        }

        PacketDistributor.TargetPoint target = new PacketDistributor.TargetPoint(sender, sender.getX(), sender.getY(), sender.getZ(), 24, sender.level().dimension());
        PacketDistributor.NEAR.with(target).send(new ClientboundNotePacket(sender.getId(), payload.status(), payload.note(), payload.instrument(), payload.octave(), payload.velocity()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.status);
        buf.writeEnum(this.note);
        buf.writeVarInt(this.instrument);
        buf.writeVarInt(this.octave);
        if (this.status == MidiInputEvent.Status.ON) {
            buf.writeVarInt(this.velocity);
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
