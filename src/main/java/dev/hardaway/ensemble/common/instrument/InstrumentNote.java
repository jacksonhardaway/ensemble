package dev.hardaway.ensemble.common.instrument;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum InstrumentNote implements StringRepresentable {
    C("c"),
    C_SHARP("c_sharp"),
    D("d"),
    D_SHARP("d_sharp"),
    E("e"),
    F("f"),
    F_SHARP("f_sharp"),
    G("g"),
    G_SHARP("g_sharp"),
    A("a"),
    A_SHARP("a_sharp"),
    B("b");

    public static final Codec<InstrumentNote> CODEC = StringRepresentable.fromEnum(InstrumentNote::values);
    private static final InstrumentNote[] NOTES = values();

    private final String key;

    InstrumentNote(String key) {
        this.key = key;
    }

    @Nullable
    public static InstrumentNote from(int note) {
        if (note < 0 || note >= NOTES.length)
            return null;
        return NOTES[note];
    }

    @Override
    public String getSerializedName() {
        return key;
    }
}
