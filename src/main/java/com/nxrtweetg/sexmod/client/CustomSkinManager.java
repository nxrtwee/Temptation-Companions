package com.nxrtweetg.sexmod.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.nxrtweetg.sexmod.girls.custom.TCEditorLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
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
    public static final ResourceLocation DEFAULT_SKIN = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/player/steve.png");
    private static final Map<String, ResourceLocation> LOADED_SKINS = new ConcurrentHashMap<>();

    private CustomSkinManager() {}

    public static ResourceLocation getOrLoadSkin(String customId, String skinFileName) {
        if (customId == null || customId.isBlank()) {
            return DEFAULT_SKIN;
        }

        ResourceLocation existing = LOADED_SKINS.get(customId);
        if (existing != null) {
            return existing;
        }

        Path path = TCEditorLoader.getSkinsDir().resolve(skinFileName != null ? skinFileName : customId + ".png");
        if (!Files.exists(path)) {
            return DEFAULT_SKIN;
        }

        try (InputStream stream = Files.newInputStream(path)) {
            NativeImage image = NativeImage.read(stream);
            image = ensureStandardFormat(image);

            String safeId = customId.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
            ResourceLocation textureId = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/custom_skin_" + safeId);

            TextureManager tm = Minecraft.getInstance().getTextureManager();
            tm.release(textureId);

            DynamicTexture dynamicTexture = new DynamicTexture(image);
            dynamicTexture.upload();
            tm.register(textureId, dynamicTexture);

            LOADED_SKINS.put(customId, textureId);
            LOGGER.info("Registered dynamic skin texture for custom girl: {}", textureId);
            return textureId;
        } catch (Exception e) {
            LOGGER.error("Failed to load skin image from {}", path, e);
            return DEFAULT_SKIN;
        }
    }

    private static NativeImage ensureStandardFormat(NativeImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width == 64 && height == 64) {
            return source;
        }
        if (width == 64 && height == 32) {
            NativeImage converted = new NativeImage(64, 64, true);
            for (int x = 0; x < 64; x++) {
                for (int y = 0; y < 32; y++) {
                    converted.setPixelRGBA(x, y, source.getPixelRGBA(x, y));
                }
            }
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    converted.setPixelRGBA(x + 16, y + 48, source.getPixelRGBA(x, y + 16));
                    converted.setPixelRGBA(x + 32, y + 48, source.getPixelRGBA(x + 40, y + 16));
                }
            }
            source.close();
            return converted;
        }
        return source;
    }
}
