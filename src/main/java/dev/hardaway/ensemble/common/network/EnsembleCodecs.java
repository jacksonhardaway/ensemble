package dev.hardaway.ensemble.common.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnsembleCodecs {

    public static final Codec<MidiFileFormat> MIDI_FILE_FORMAT = RecordCodecBuilder.create(instance -> instance.group(
            // We only really care about these properties.
            // If needed, we can support more in the future.
            Codec.STRING.optionalFieldOf("author").forGetter(file -> {
                Object author = file.getProperty("author");
                if (author instanceof String s)
                    return Optional.of(s);
                return Optional.empty();
            }),
            Codec.STRING.optionalFieldOf("title").forGetter(file -> {
                Object author = file.getProperty("title");
                if (author instanceof String s)
                    return Optional.of(s);
                return Optional.empty();
            }),

            Codec.INT.fieldOf("f").forGetter(MidiFileFormat::getType),
            Codec.FLOAT.fieldOf("d").forGetter(MidiFileFormat::getDivisionType),
            Codec.INT.fieldOf("r").forGetter(MidiFileFormat::getResolution),
            Codec.INT.fieldOf("b").forGetter(MidiFileFormat::getByteLength),
            Codec.LONG.fieldOf("ms").forGetter(MidiFileFormat::getMicrosecondLength)
    ).apply(instance, (author, title, fileType, divisionType, timingResolution, byteLength, msLength) -> {
        Map<String, Object> properties = new HashMap<>();
        author.ifPresent(value -> properties.put("author", author));
        title.ifPresent(value -> properties.put("title", title));

        return new MidiFileFormat(fileType, divisionType, timingResolution, byteLength, timingResolution, properties);
    }));

    public static final Codec<ShortMessage> SHORT_MIDI_MESSAGE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("s").forGetter(ShortMessage::getStatus),
            Codec.INT.listOf().fieldOf("d").forGetter(message -> List.of(message.getData1(), message.getData2()))
    ).apply(instance, (status, data) -> {
        try {
            return new ShortMessage(status, data.get(0), data.get(1));
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e); // TODO: better exception
        }
    }));

    public static final Codec<MetaMessage> META_MIDI_MESSAGE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("t").forGetter(MetaMessage::getType),
            Codec.INT.fieldOf("l").forGetter(MetaMessage::getLength),
            Codec.BYTE_BUFFER.xmap(ByteBuffer::array, ByteBuffer::wrap).fieldOf("d").forGetter(MetaMessage::getData)
    ).apply(instance, (type, length, data) -> {
        try {
            return new MetaMessage(type, data, length);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }));

    public static final Codec<SysexMessage> SYSEX_MIDI_MESSAGE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("l").forGetter(SysexMessage::getLength),
            Codec.BYTE_BUFFER.xmap(ByteBuffer::array, ByteBuffer::wrap).fieldOf("d").forGetter(SysexMessage::getData)
    ).apply(instance, (length, data) -> {
        try {
            return new SysexMessage(data, length);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }));

    public static final Codec<MidiMessage> MIDI_MESSAGE = MidiMessageType.CODEC.dispatch("m", message -> {
        if (message instanceof ShortMessage) {
            return MidiMessageType.SHORT;
        } else if (message instanceof SysexMessage) {
            return MidiMessageType.SYSEX;
        } else if (message instanceof MetaMessage) {
            return MidiMessageType.META;
        }

        // This should never happen.
        return MidiMessageType.UNKNOWN;
    }, MidiMessageType::getCodec);

    public static final Codec<MidiEvent> MIDI_EVENT = RecordCodecBuilder.create(instance -> instance.group(
            EnsembleCodecs.MIDI_MESSAGE.fieldOf("m").forGetter(MidiEvent::getMessage),
            Codec.LONG.fieldOf("t").forGetter(MidiEvent::getTick)
    ).apply(instance, MidiEvent::new));

    public static final Codec<Sequence> SEQUENCE = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("d").forGetter(Sequence::getDivisionType),
            Codec.INT.fieldOf("r").forGetter(Sequence::getResolution),
            EnsembleCodecs.MIDI_EVENT.listOf().listOf().fieldOf("t").forGetter(sequence -> {
                List<List<MidiEvent>> tracks = new ArrayList<>();
                for (Track track : sequence.getTracks()) {
                    List<MidiEvent> events = new ArrayList<>();
                    for (int i = 0; i < track.size(); i++) {
                        events.add(track.get(i));
                    }
                    tracks.add(events);
                }

                return tracks;
            })
    ).apply(instance, (divisionType, resolution, tracks) -> {
        try {
            Sequence sequence  = new Sequence(divisionType, resolution);
            for (List<MidiEvent> trackEvents : tracks) {
                Track track = sequence.createTrack();
                trackEvents.forEach(track::add);
            }
            return sequence;
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e); // TODO: better exception
        }
    }));

    public enum MidiMessageType {
        SHORT(EnsembleCodecs.SHORT_MIDI_MESSAGE),
        SYSEX(EnsembleCodecs.SYSEX_MIDI_MESSAGE),
        META(EnsembleCodecs.META_MIDI_MESSAGE),
        UNKNOWN(Codec.unit(null));

        public static final Codec<MidiMessageType> CODEC = Codec.INT.xmap(integer -> MidiMessageType.values()[integer], Enum::ordinal);
        private final Codec<? extends MidiMessage> codec;

        MidiMessageType(Codec<? extends MidiMessage> codec) {
            this.codec = codec;
        }

        public Codec<? extends MidiMessage> getCodec() {
            return codec;
        }
    }
}
