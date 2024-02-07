package dev.hardaway.ensemble.common.instrument;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum InstrumentNote implements StringRepresentable {
    C("c", true),
    C_SHARP("c_sharp", false),
    D("d", true),
    D_SHARP("d_sharp", false),
    E("e", false),
    F("f", true),
    F_SHARP("f_sharp", false),
    G("g", true),
    G_SHARP("g_sharp", false),
    A("a", true),
    A_SHARP("a_sharp", false),
    B("b", false);

    public static final Codec<InstrumentNote> CODEC = StringRepresentable.fromEnum(InstrumentNote::values);
    private static final InstrumentNote[] NOTES = values();

    private final String key;
    private final boolean canSharpen;

    InstrumentNote(String key, boolean canSharpen) {
        this.key = key;
        this.canSharpen = canSharpen;
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

    public boolean canSharpen() {
        return canSharpen;
    }

    public InstrumentNote sharpen() {
        if (!this.canSharpen) {
            return this;
        }
        return InstrumentNote.values()[this.ordinal() + 1];
    }
}
