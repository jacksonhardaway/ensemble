package dev.hardaway.ensemble.common.instrument.sheetmusic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import dev.hardaway.ensemble.common.network.protocol.ClientboundSyncSheetMusicPacket;
import net.minecraft.Util;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class SheetMusicManager extends SimplePreparableReloadListener<Map<ResourceLocation, SheetMusic>> {
    public static final SheetMusicManager INSTANCE = new SheetMusicManager();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String DIRECTORY = "sheet_music";

    private final Map<ResourceLocation, SheetMusic> sheetMusic = new HashMap<>();

    private SheetMusicManager() {
    }

    public static void register(AddReloadListenerEvent event) {
        event.addListener(SheetMusicManager.INSTANCE);
    }

    // TODO: configuration opt: operators or everyone
    public void add(ResourceLocation id, SheetMusic sheetMusic) {
        this.sheetMusic.put(id, sheetMusic);
    }

    // TODO: only operators can remove sheet music
    public void remove(ResourceLocation id) {
        this.sheetMusic.remove(id);
    }

    // TODO: on join, sync sheet music
    public void sync(Consumer<CustomPacketPayload> sender) {
        sender.accept(new ClientboundSyncSheetMusicPacket(this.sheetMusic));
    }

    public void syncSheetMusic(Map<ResourceLocation, SheetMusic> sheetMusic) {
        this.sheetMusic.clear();
        this.sheetMusic.putAll(sheetMusic);
        // TODO: sync with clients
//        PacketDistributor.ALL.noArg().send(new ClientboundSyncSheetMusicPacket(this.sheetMusic));
    }

    @Override
    protected void apply(Map<ResourceLocation, SheetMusic> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.syncSheetMusic(entries);
        LOGGER.info("Loaded {} sheet music", this.sheetMusic.size());
    }

    @Override
    protected Map<ResourceLocation, SheetMusic> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, SheetMusic> map = new HashMap<>();
        scanJson(resourceManager, map);
        scanMid(resourceManager, map);
        return map;
    }

    private static void scanJson(ResourceManager resourceManager, Map<ResourceLocation, SheetMusic> output) {
        FileToIdConverter idConverter = FileToIdConverter.json(SheetMusicManager.DIRECTORY);

        for (Map.Entry<ResourceLocation, Resource> entry : idConverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation key = entry.getKey();
            ResourceLocation id = idConverter.fileToId(key);

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = GsonHelper.fromJson(SheetMusicManager.GSON, reader, JsonElement.class);
                SheetMusic sheetMusic = Util.getOrThrow(SheetMusic.CODEC.parse(JsonOps.INSTANCE, json), JsonParseException::new);
                if (output.put(id, sheetMusic) != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                LOGGER.error("Couldn't parse data file {} from {}", id, key, e);
            }
        }
    }

    private static void scanMid(ResourceManager resourceManager, Map<ResourceLocation, SheetMusic> output) {
        FileToIdConverter idConverter = new FileToIdConverter(SheetMusicManager.DIRECTORY, ".mid");

        for (Map.Entry<ResourceLocation, Resource> entry : idConverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation key = entry.getKey();
            ResourceLocation id = idConverter.fileToId(key);

            // TODO: size limit in config
            try (InputStream stream = new BufferedInputStream(entry.getValue().open())) {
                MidiFileFormat format = MidiSystem.getMidiFileFormat(stream);
                Sequence sequence = MidiSystem.getSequence(stream);

                SheetMusic sheetMusic = new SheetMusic(format, sequence);
                if (output.put(id, sheetMusic) != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                }
            } catch (IllegalArgumentException | IOException | InvalidMidiDataException e) {
                LOGGER.error("Couldn't parse data file {} from {}", id, key, e);
            }
        }
    }
}
