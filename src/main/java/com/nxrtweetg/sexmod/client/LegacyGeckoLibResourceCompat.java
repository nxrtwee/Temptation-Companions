package com.nxrtweetg.sexmod.client;

import com.geckolib.cache.BakedAnimationCache;
import com.geckolib.cache.BakedModelCache;
import com.geckolib.cache.GeckoLibResources;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MathParser;
import com.google.gson.JsonObject;
import com.nxrtweetg.sexmod.SexmodMod;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener.SharedState;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public final class LegacyGeckoLibResourceCompat {
   private static final Identifier LISTENER_ID = Identifier.fromNamespaceAndPath("sexmod", "legacy_geckolib_resource_compat");
   private static final Identifier GECKOLIB_LISTENER_ID = Identifier.fromNamespaceAndPath("geckolib", "geckolib_resources");
   private static final String LEGACY_MODEL_ROOT = "geo";
   private static final String LEGACY_ANIMATION_ROOT = "animations";
   private static final String MODERN_MODEL_ROOT = "geckolib/models/";
   private static final String MODERN_ANIMATION_ROOT = "geckolib/animations/";
   private static final String MODEL_SUFFIX = ".geo.json";
   private static final String ANIMATION_SUFFIX = ".animation.json";
   private static final Map<String, List<String>> LEGACY_MODEL_ALIASES = Map.of(
      "cat/cat", List.of("cat/dressed", "cat/nude"),
      "ellie/ellie", List.of("ellie/dressed", "ellie/nude"),
      "slime/slime", List.of("slime/dressed", "slime/nude")
   );

   private LegacyGeckoLibResourceCompat() {
   }

   public static void initialize() {
      ResourceLoader loader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
      loader.registerReloadListener(LISTENER_ID, new LegacyResourceReloadListener());
      loader.addListenerOrdering(GECKOLIB_LISTENER_ID, LISTENER_ID);
   }

   private static Identifier normalizeLegacyModelId(Identifier resourceId) {
      return normalizeLegacyId(resourceId, LEGACY_MODEL_ROOT, MODEL_SUFFIX);
   }

   private static Identifier normalizeLegacyAnimationId(Identifier resourceId) {
      return normalizeLegacyId(resourceId, LEGACY_ANIMATION_ROOT, ANIMATION_SUFFIX);
   }

   private static Identifier normalizeLegacyId(Identifier resourceId, String root, String suffix) {
      String path = resourceId.getPath();
      String prefix = root + "/";
      if (!path.startsWith(prefix) || !path.endsWith(suffix)) {
         return null;
      }

      return resourceId.withPath(path.substring(prefix.length(), path.length() - suffix.length()));
   }

   private static Identifier toModernModelResourceId(Identifier cacheId) {
      return cacheId.withPath(MODERN_MODEL_ROOT + cacheId.getPath() + MODEL_SUFFIX);
   }

   private static Identifier toModernAnimationResourceId(Identifier cacheId) {
      return cacheId.withPath(MODERN_ANIMATION_ROOT + cacheId.getPath() + ANIMATION_SUFFIX);
   }

   private static boolean isJsonFile(Identifier resourceId) {
      return resourceId.getPath().endsWith(".json");
   }

   private static void setGeckoLibCache(String fieldName, Object value) {
      try {
         Field field = GeckoLibResources.class.getDeclaredField(fieldName);
         field.setAccessible(true);
         field.set(null, value);
      } catch (ReflectiveOperationException exception) {
         throw new IllegalStateException("Failed to update GeckoLib cache: " + fieldName, exception);
      }
   }

   private record PreparedLegacyResources(
      Map<Identifier, BakedGeoModel> models,
      Map<Identifier, BakedAnimations> animations
   ) {
   }

   private static final class LegacyResourceReloadListener extends SimpleReloadListener<PreparedLegacyResources> {
      @Override
      protected PreparedLegacyResources prepare(SharedState sharedState) {
         ResourceManager resourceManager = sharedState.resourceManager();
         GeckoLibGsonLoader loader = new GeckoLibGsonLoader();
         MathParser mathParser = MathParser.createWithDeduplication();
         Map<Identifier, BakedGeoModel> models = new LinkedHashMap<>();
         Map<Identifier, BakedAnimations> animations = new LinkedHashMap<>();

         for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(LEGACY_MODEL_ROOT, LegacyGeckoLibResourceCompat::isJsonFile).entrySet()) {
            Identifier cacheId = normalizeLegacyModelId(entry.getKey());
            if (cacheId == null) {
               continue;
            }

            try {
               Identifier modernId = toModernModelResourceId(cacheId);
               JsonObject json = loader.deserializeGeckoLibModelFile(modernId, entry.getValue());
               BakedGeoModel model = loader.bakeGeckoLibModelFile(modernId, json);
               models.put(cacheId, model);

               for (String aliasPath : LEGACY_MODEL_ALIASES.getOrDefault(cacheId.getPath(), List.of())) {
                  models.putIfAbsent(cacheId.withPath(aliasPath), model);
               }
            } catch (RuntimeException exception) {
               SexmodMod.LOGGER.warn("Failed to load legacy model resource {}", entry.getKey(), exception);
            }
         }

         for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(LEGACY_ANIMATION_ROOT, LegacyGeckoLibResourceCompat::isJsonFile).entrySet()) {
            Identifier cacheId = normalizeLegacyAnimationId(entry.getKey());
            if (cacheId == null) {
               continue;
            }

            try {
               Identifier modernId = toModernAnimationResourceId(cacheId);
               JsonObject json = loader.deserializeGeckoLibAnimationFile(modernId, entry.getValue());
               BakedAnimations bakedAnimations = loader.bakeGeckoLibAnimationsFile(modernId, json, mathParser);
               animations.put(cacheId, bakedAnimations);
            } catch (RuntimeException exception) {
               SexmodMod.LOGGER.warn("Failed to load legacy animation resource {}", entry.getKey(), exception);
            }
         }

         return new PreparedLegacyResources(models, animations);
      }

      @Override
      protected void apply(PreparedLegacyResources prepared, SharedState sharedState) {
         if (prepared.models.isEmpty() && prepared.animations.isEmpty()) {
            return;
         }

         Map<Identifier, BakedGeoModel> mergedModels = new LinkedHashMap<>(GeckoLibResources.getBakedModels().cache());
         mergedModels.putAll(prepared.models);
         Map<Identifier, BakedAnimations> mergedAnimations = new LinkedHashMap<>(GeckoLibResources.getBakedAnimations().cache());
         prepared.animations.forEach((cacheId, packed) -> mergedAnimations.merge(cacheId, packed, LegacyResourceReloadListener::overlay));

         setGeckoLibCache("MODELS", new BakedModelCache(mergedModels));
         setGeckoLibCache("ANIMATIONS", new BakedAnimationCache(mergedAnimations));

         SexmodMod.LOGGER.info(
            "Loaded legacy resource-pack compatibility entries: {} models, {} animations",
            prepared.models.size(),
            prepared.animations.size()
         );
      }

      /**
       * A pack's take on an animation wins, but animations it does not ship keep ours. 1.12.2 packs
       * predate scenes we added, so replacing the file wholesale would leave those animations
       * missing and crash the renderer when a scene asks for one.
       */
      private static BakedAnimations overlay(BakedAnimations ours, BakedAnimations fromPack) {
         Map<String, Animation> merged = new LinkedHashMap<>(ours.animations());
         merged.putAll(fromPack.animations());
         return new BakedAnimations(merged);
      }
   }
}
