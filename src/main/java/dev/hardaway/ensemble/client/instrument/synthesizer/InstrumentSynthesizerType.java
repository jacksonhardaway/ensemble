package dev.hardaway.ensemble.client.instrument.synthesizer;

import com.mojang.serialization.Codec;

public interface InstrumentSynthesizerType<T extends InstrumentSynthesizer> {

    static <T extends InstrumentSynthesizer> InstrumentSynthesizerType<T> of(Codec<T> codec) {
        return () -> codec;
    }

    Codec<T> codec();
}
