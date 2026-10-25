package com.nxrtweetg.sexmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.nxrtweetg.sexmod.girls.custom.TCEditorLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public final class CustomSkinManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("SexMod-CustomSkin");
    public static final Identifier DEFAULT_SKIN = Identifier.fromNamespaceAndPath("sexmod", "textures/player/steve.png");
    private static final Map<String, Identifier> LOADED_SKINS = new ConcurrentHashMap<>();
    private static final Map<String, Long> SKIN_TIMESTAMPS = new ConcurrentHashMap<>();

    private CustomSkinManager() {}

    public static boolean isCustomCharacter(String character) {
        return character != null && (character.startsWith("custom_") || "custom".equals(character));
    }

    public static Identifier getOrLoadSkin(String customId, String skinFileName) {
        if (customId == null || customId.isBlank()) {
            return DEFAULT_SKIN;
        }

        Path path = TCEditorLoader.getSkinsDir().resolve(skinFileName != null ? skinFileName : customId + ".png");
        if (!Files.exists(path)) {
            return DEFAULT_SKIN;
        }

        try {
            long lastMod = Files.getLastModifiedTime(path).toMillis();
            Identifier existing = LOADED_SKINS.get(customId);
            Long cachedMod = SKIN_TIMESTAMPS.get(customId);
            if (existing != null && cachedMod != null && lastMod <= cachedMod) {
                return existing;
            }

            try (InputStream stream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(stream);
                image = ensureStandardFormat(image);

                String safeId = customId.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
                Identifier textureId = Identifier.fromNamespaceAndPath("sexmod", "textures/custom_skin_" + safeId);

                TextureManager tm = Minecraft.getInstance().getTextureManager();
                tm.release(textureId);

                DynamicTexture dynamicTexture = new DynamicTexture(() -> "TC-Skin-" + safeId, image);
                dynamicTexture.upload();
                tm.register(textureId, dynamicTexture);

                LOADED_SKINS.put(customId, textureId);
                SKIN_TIMESTAMPS.put(customId, lastMod);
                LOGGER.info("Registered dynamic skin texture for custom girl: {}", textureId);
                return textureId;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load skin image from {}", path, e);
            return DEFAULT_SKIN;
        }
    }

    private static NativeImage ensureStandardFormat(NativeImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        NativeImage img = source;

        if (width == 64 && height == 32) {
            NativeImage converted = new NativeImage(64, 64, true);
            for (int x = 0; x < 64; x++) {
                for (int y = 0; y < 32; y++) {
                    converted.setPixel(x, y, source.getPixel(x, y));
                }
            }
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    converted.setPixel(x + 16, y + 48, source.getPixel(x, y + 16));
                    converted.setPixel(x + 32, y + 48, source.getPixel(x + 40, y + 16));
                }
            }
            source.close();
            img = converted;
        }

        // Set unused region (0, 0)..(3, 3) to pure opaque white for cosmetics palette tinting
        for (int x = 0; x <= 3; x++) {
            for (int y = 0; y <= 3; y++) {
                img.setPixel(x, y, 0xFFFFFFFF);
            }
        }

        return img;
    }
}
