package dev.hardaway.forte.client.instrument.synthesizer;

import com.mojang.serialization.Codec;

public interface InstrumentSynthesizerType {

    static InstrumentSynthesizerType of(Codec<? extends InstrumentSynthesizer> codec) {
        return () -> codec;
    }

    Codec<? extends InstrumentSynthesizer> codec();
}
