package dev.hardaway.ensemble.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import net.minecraft.client.KeyMapping;

public class NoteKeyMapping extends KeyMapping {

    private final InstrumentNote note;
    private int currentOctave = 0;
    private boolean sharpened = false;

    public NoteKeyMapping(InstrumentNote note, int key) {
        super("key." + Ensemble.MOD_ID + ".note." + note.getSerializedName(),
                EnsembleKeyMappings.PLAYING_INSTRUMENT_CONFLICT,
                InputConstants.Type.KEYSYM,
                key,
                "key.categories." + Ensemble.MOD_ID
        );
        this.note = note;
    }

    @Override
    public void setDown(boolean pValue) {
        if (!pValue) {

        }
        super.setDown(pValue);
    }

    public InstrumentNote getNote() {
        return note;
    }

    public void setSharpened(boolean sharpened) {
        this.sharpened = sharpened;
    }

    public boolean isSharpened() {
        return sharpened;
    }

    public void setOctave(int currentOctave) {
        this.currentOctave = currentOctave;
    }

    public int getOctave() {
        return currentOctave;
    }
}
