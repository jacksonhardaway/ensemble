package dev.hardaway.forte.common.instrument;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import dev.hardaway.forte.Forte;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Forte.MOD_ID)
public class InstrumentManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final InstrumentManager INSTANCE = new InstrumentManager();

    private Map<ResourceLocation, InstrumentDefinition> instruments = ImmutableMap.of();

    public InstrumentManager() {
        super(InstrumentManager.GSON, "instrument_definition");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, InstrumentDefinition> map = ImmutableMap.builder();

        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            InstrumentDefinition instrument = Util.getOrThrow(InstrumentDefinition.CODEC.parse(JsonOps.INSTANCE, entry.getValue()), JsonParseException::new);
            map.put(entry.getKey(), instrument);
        }

        this.instruments = map.build();
        LOGGER.info("Loaded {} instruments", this.instruments.size());
    }

    @Nullable
    public InstrumentDefinition getInstrument(ResourceLocation id) {
        return instruments.get(id);
    }
}
