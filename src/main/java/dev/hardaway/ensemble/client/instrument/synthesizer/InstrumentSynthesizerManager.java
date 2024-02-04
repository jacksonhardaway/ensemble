package dev.hardaway.ensemble.client.instrument.synthesizer;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;

public class InstrumentSynthesizerManager extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Map<ResourceLocation, ? extends InstrumentSynthesizer> synthesizers = ImmutableMap.of();

    public InstrumentSynthesizerManager() {
        super(InstrumentSynthesizerManager.GSON, "instrument_synthesizers");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, InstrumentSynthesizer> map = ImmutableMap.builder();

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            InstrumentSynthesizer synthesizer;
            try {
                synthesizer = Util.getOrThrow(InstrumentSynthesizer.CODEC.parse(JsonOps.INSTANCE, entry.getValue()), JsonParseException::new);
            } catch (JsonParseException e) {
                LOGGER.warn("Unable to load synthesizer: '" + entry.getKey() + "'", e);
                continue;
            }
            map.put(entry.getKey(), synthesizer);
        }

        this.synthesizers = map.build();
        LOGGER.info("Loaded {} synthesizers", this.synthesizers.size());
    }

    public @Nullable InstrumentSynthesizer getSynthesizer(ResourceLocation id) {
        return this.synthesizers.get(id);
    }
}
