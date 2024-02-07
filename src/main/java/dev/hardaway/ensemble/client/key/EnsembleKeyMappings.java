package dev.hardaway.ensemble.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import dev.hardaway.ensemble.client.ClientConductor;
import net.neoforged.neoforge.common.util.Lazy;
import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.instrument.InstrumentNote;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EnsembleKeyMappings {

    public static final IKeyConflictContext PLAYING_INSTRUMENT_CONFLICT = new IKeyConflictContext() {
        @Override
        public boolean isActive() {
            return ClientConductor.INSTANCE.getRecordingInstrument() != null;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    };

    private static final Set<NoteKeyMapping> NOTE_KEYS = new HashSet<>();
    private static final Set<NoteKeyMapping> NOTE_KEY_VIEW = Collections.unmodifiableSet(NOTE_KEYS);

    public static final Lazy<KeyMapping> SHARPEN_NOTE = Lazy.of(() -> createInstrumentKey("sharpen_note", GLFW.GLFW_KEY_KP_0));
    public static final Lazy<KeyMapping> TRANSPOSE_OCTAVE_UP = Lazy.of(() -> createInstrumentKey("transpose_octave_up", GLFW.GLFW_KEY_KP_ADD));
    public static final Lazy<KeyMapping> TRANSPOSE_OCTAVE_DOWN = Lazy.of(() -> createInstrumentKey("transpose_octave_down", GLFW.GLFW_KEY_KP_ENTER));

    public static final Lazy<NoteKeyMapping> NOTE_C = Lazy.of(() -> createNoteKey(InstrumentNote.C, GLFW.GLFW_KEY_KP_1));
    public static final Lazy<NoteKeyMapping> NOTE_D = Lazy.of(() -> createNoteKey(InstrumentNote.D, GLFW.GLFW_KEY_KP_2));
    public static final Lazy<NoteKeyMapping> NOTE_E = Lazy.of(() -> createNoteKey(InstrumentNote.E, GLFW.GLFW_KEY_KP_3));
    public static final Lazy<NoteKeyMapping> NOTE_F = Lazy.of(() -> createNoteKey(InstrumentNote.F, GLFW.GLFW_KEY_KP_4));
    public static final Lazy<NoteKeyMapping> NOTE_G = Lazy.of(() -> createNoteKey(InstrumentNote.G, GLFW.GLFW_KEY_KP_5));
    public static final Lazy<NoteKeyMapping> NOTE_A = Lazy.of(() -> createNoteKey(InstrumentNote.A, GLFW.GLFW_KEY_KP_6));
    public static final Lazy<NoteKeyMapping> NOTE_B = Lazy.of(() -> createNoteKey(InstrumentNote.B, GLFW.GLFW_KEY_KP_7));

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(EnsembleKeyMappings.SHARPEN_NOTE.get());
        event.register(EnsembleKeyMappings.TRANSPOSE_OCTAVE_UP.get());
        event.register(EnsembleKeyMappings.TRANSPOSE_OCTAVE_DOWN.get());
        event.register(EnsembleKeyMappings.NOTE_C.get());
        event.register(EnsembleKeyMappings.NOTE_D.get());
        event.register(EnsembleKeyMappings.NOTE_E.get());
        event.register(EnsembleKeyMappings.NOTE_F.get());
        event.register(EnsembleKeyMappings.NOTE_G.get());
        event.register(EnsembleKeyMappings.NOTE_A.get());
        event.register(EnsembleKeyMappings.NOTE_B.get());
    }

    public static Set<NoteKeyMapping> getNoteKeySet() {
        return NOTE_KEY_VIEW;
    }

    private static NoteKeyMapping createNoteKey(InstrumentNote note, int key) {
        NoteKeyMapping keyMapping = new NoteKeyMapping(note, key);
        NOTE_KEYS.add(keyMapping);
        return new NoteKeyMapping(note, key);
    }

    private static KeyMapping createInstrumentKey(String name, int key) {
        return new KeyMapping(
                "key." + Ensemble.MOD_ID + "." + name,
                EnsembleKeyMappings.PLAYING_INSTRUMENT_CONFLICT,
                InputConstants.Type.KEYSYM,
                key,
                "key.categories." + Ensemble.MOD_ID
        );
    }

}
