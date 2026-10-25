package com.nxrtweetg.sexmod.girls.custom;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class TCEditorLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("SexMod-TCEditorLoader");
    private static final Map<String, CustomCharacterData> CHARACTERS = new ConcurrentHashMap<>();
    private static final Map<String, Long> FILE_TIMESTAMPS = new ConcurrentHashMap<>();

    public record CustomCharacterData(
        String id,
        String name,
        String modelType,
        String skinFileName,
        float scale,
        String baseCharacter,
        float headScaleX,
        float headScaleY,
        float headScaleZ,
        float torsoScaleX,
        float torsoScaleY,
        float torsoScaleZ,
        float armScaleX,
        float armScaleY,
        float armScaleZ,
        float legScaleX,
        float legScaleY,
        float legScaleZ,
        boolean hasBreasts,
        float breastScale,
        boolean hasBooty,
        float bootyScale,
        int cosmeticsColor,
        List<String> enabledScenes,
        List<String> allowedPhases
    ) {}

    private TCEditorLoader() {}

    public static Path getEditorConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("tceditor");
    }

    public static Path getCharactersDir() {
        return getEditorConfigDir().resolve("characters");
    }

    public static Path getSkinsDir() {
        return getEditorConfigDir().resolve("skins");
    }

    public static void reload() {
        CHARACTERS.clear();
        FILE_TIMESTAMPS.clear();
        Path charsDir = getCharactersDir();
        if (!Files.exists(charsDir)) {
            try {
                Files.createDirectories(charsDir);
            } catch (Exception ignored) {}
            return;
        }

        try (Stream<Path> stream = Files.list(charsDir)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(TCEditorLoader::loadCharacterFile);
        } catch (Exception e) {
            LOGGER.error("Failed to load characters from {}", charsDir, e);
        }
        LOGGER.info("Loaded {} custom characters from TC-Editor", CHARACTERS.size());
    }

    private static void loadCharacterFile(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String id = json.has("id") ? json.get("id").getAsString() : path.getFileName().toString().replace(".json", "");
            String name = json.has("name") ? json.get("name").getAsString() : id;
            String modelType = json.has("modelType") ? json.get("modelType").getAsString() : "classic";
            String skinFileName = json.has("skinFileName") ? json.get("skinFileName").getAsString() : id + ".png";
            float scale = json.has("scale") ? json.get("scale").getAsFloat() : 1.0f;
            String baseCharacter = json.has("baseCharacter") ? json.get("baseCharacter").getAsString() : "custom";

            float headScaleX = json.has("headScaleX") ? json.get("headScaleX").getAsFloat() : 1.0f;
            float headScaleY = json.has("headScaleY") ? json.get("headScaleY").getAsFloat() : 1.0f;
            float headScaleZ = json.has("headScaleZ") ? json.get("headScaleZ").getAsFloat() : 1.0f;

            float torsoScaleX = json.has("torsoScaleX") ? json.get("torsoScaleX").getAsFloat() : 1.0f;
            float torsoScaleY = json.has("torsoScaleY") ? json.get("torsoScaleY").getAsFloat() : 1.0f;
            float torsoScaleZ = json.has("torsoScaleZ") ? json.get("torsoScaleZ").getAsFloat() : 1.0f;

            float armScaleX = json.has("armScaleX") ? json.get("armScaleX").getAsFloat() : 1.0f;
            float armScaleY = json.has("armScaleY") ? json.get("armScaleY").getAsFloat() : 1.0f;
            float armScaleZ = json.has("armScaleZ") ? json.get("armScaleZ").getAsFloat() : 1.0f;

            float legScaleX = json.has("legScaleX") ? json.get("legScaleX").getAsFloat() : 1.0f;
            float legScaleY = json.has("legScaleY") ? json.get("legScaleY").getAsFloat() : 1.0f;
            float legScaleZ = json.has("legScaleZ") ? json.get("legScaleZ").getAsFloat() : 1.0f;

            boolean hasBreasts = json.has("hasBreasts") && json.get("hasBreasts").getAsBoolean();
            float breastScale = json.has("breastScale") ? json.get("breastScale").getAsFloat() : 1.0f;
            boolean hasBooty = json.has("hasBooty") && json.get("hasBooty").getAsBoolean();
            float bootyScale = json.has("bootyScale") ? json.get("bootyScale").getAsFloat() : 1.0f;
            int cosmeticsColor = json.has("cosmeticsColor") ? json.get("cosmeticsColor").getAsInt() : 0xFFFFFFFF;

            List<String> enabledScenes = new ArrayList<>();
            if (json.has("enabledScenes") && json.get("enabledScenes").isJsonArray()) {
                for (JsonElement el : json.getAsJsonArray("enabledScenes")) {
                    enabledScenes.add(el.getAsString());
                }
            }
            if (enabledScenes.isEmpty()) {
                enabledScenes.addAll(List.of("blowjob", "boobjob", "doggy", "strip"));
            }

            List<String> allowedPhases = new ArrayList<>();
            if (json.has("allowedPhases") && json.get("allowedPhases").isJsonArray()) {
                for (JsonElement el : json.getAsJsonArray("allowedPhases")) {
                    allowedPhases.add(el.getAsString());
                }
            }
            if (allowedPhases.isEmpty()) {
                allowedPhases.addAll(List.of("waiting", "active", "climax"));
            }

            CustomCharacterData data = new CustomCharacterData(
                id, name, modelType, skinFileName, scale, baseCharacter,
                headScaleX, headScaleY, headScaleZ,
                torsoScaleX, torsoScaleY, torsoScaleZ,
                armScaleX, armScaleY, armScaleZ,
                legScaleX, legScaleY, legScaleZ,
                hasBreasts, breastScale,
                hasBooty, bootyScale,
                cosmeticsColor,
                enabledScenes, allowedPhases
            );
            CHARACTERS.put(id.toLowerCase(), data);
            CHARACTERS.put(name.toLowerCase(), data);

            try {
                long mtime = Files.getLastModifiedTime(path).toMillis();
                FILE_TIMESTAMPS.put(id.toLowerCase(), mtime);
                FILE_TIMESTAMPS.put(name.toLowerCase(), mtime);
            } catch (Exception ignored) {}
        } catch (Exception e) {
            LOGGER.error("Failed to parse character JSON at {}", path, e);
        }
    }

    public static CustomCharacterData get(String idOrName) {
        if (idOrName == null) return null;
        String key = idOrName.toLowerCase();
        Path charPath = getCharactersDir().resolve(key + ".json");
        if (Files.exists(charPath)) {
            try {
                long lastMod = Files.getLastModifiedTime(charPath).toMillis();
                Long cached = FILE_TIMESTAMPS.get(key);
                if (cached == null || lastMod > cached) {
                    loadCharacterFile(charPath);
                }
            } catch (Exception ignored) {}
        }
        CustomCharacterData data = CHARACTERS.get(key);
        if (data == null) {
            reload();
            data = CHARACTERS.get(key);
        }
        return data;
    }

    public static Collection<CustomCharacterData> getAll() {
        if (CHARACTERS.isEmpty()) {
            reload();
        }
        return new HashSet<>(CHARACTERS.values());
    }

    public static Set<String> getAvailableNames() {
        if (CHARACTERS.isEmpty()) {
            reload();
        }
        Set<String> names = new TreeSet<>();
        for (CustomCharacterData c : CHARACTERS.values()) {
            names.add(c.id());
        }
        return names;
    }
}
