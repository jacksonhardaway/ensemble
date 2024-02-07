package dev.hardaway.ensemble.common.network.protocol;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.client.ClientConductor;
import dev.hardaway.ensemble.client.midi.MidiEvent;
import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record ClientboundNotePacket(int entityId,
                                    MidiEvent.Status status,
                                    InstrumentNote note,
                                    int instrument,
                                    int octave,
                                    int velocity) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(Ensemble.MOD_ID, "clientbound_note");

    public static ClientboundNotePacket read(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        MidiEvent.Status status = buf.readEnum(MidiEvent.Status.class);
        InstrumentNote note = buf.readEnum(InstrumentNote.class);
        int instrument = buf.readVarInt();
        int octave = buf.readVarInt();
        int velocity = status == MidiEvent.Status.ON ? buf.readVarInt() : 0;
        return new ClientboundNotePacket(entityId, status, note, instrument, octave, velocity);
    }

    public static void handle(ClientboundNotePacket payload, PlayPayloadContext ctx) {
        ClientConductor.INSTANCE.handleNote(payload, ctx);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeEnum(this.status);
        buf.writeEnum(this.note);
        buf.writeVarInt(this.instrument);
        buf.writeVarInt(this.octave);
        if (this.status == MidiEvent.Status.ON) {
            buf.writeVarInt(this.velocity);
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
