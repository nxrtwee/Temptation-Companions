package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.BakedAnimationsAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;
import com.google.gson.GsonBuilder;
import com.nxrtweetg.sexmod.SexmodMod;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationLengths;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public final class LegacyGeckoLibResourceCompat {
   private static final ResourceLocation LISTENER_ID = ResourceLocation.fromNamespaceAndPath("sexmod", "legacy_geckolib_resource_compat");
   private static final ResourceLocation GECKOLIB_LISTENER_ID = ResourceLocation.fromNamespaceAndPath("geckolib", "geckolib_resources");
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
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new LegacyResourceReloadListener());
   }

   private static ResourceLocation normalizeLegacyModelId(ResourceLocation resourceId) {
      return normalizeLegacyId(resourceId, LEGACY_MODEL_ROOT, MODEL_SUFFIX);
   }

   private static ResourceLocation normalizeLegacyAnimationId(ResourceLocation resourceId) {
      return normalizeLegacyId(resourceId, LEGACY_ANIMATION_ROOT, ANIMATION_SUFFIX);
   }

   private static ResourceLocation normalizeLegacyId(ResourceLocation resourceId, String root, String suffix) {
      String path = resourceId.getPath();
      String prefix = root + "/";
      if (!path.startsWith(prefix) || !path.endsWith(suffix)) {
         return null;
      }

      return resourceId.withPath(path.substring(prefix.length(), path.length() - suffix.length()));
   }

   private static ResourceLocation toModernModelResourceId(ResourceLocation cacheId) {
      return cacheId.withPath(MODERN_MODEL_ROOT + cacheId.getPath() + MODEL_SUFFIX);
   }

   private static ResourceLocation toModernAnimationResourceId(ResourceLocation cacheId) {
      return cacheId.withPath(MODERN_ANIMATION_ROOT + cacheId.getPath() + ANIMATION_SUFFIX);
   }

   private static boolean isJsonFile(ResourceLocation resourceId) {
      return resourceId.getPath().endsWith(".json");
   }

   /** GeckoLib 5.2 keeps its baked resources in two plain static maps, swapped wholesale here. */
   private static void setGeckoLibCache(String fieldName, Object value) {
      try {
         Field field = GeckoLibCache.class.getDeclaredField(fieldName);
         field.setAccessible(true);
         field.set(null, value);
      } catch (ReflectiveOperationException exception) {
         throw new IllegalStateException("Failed to update GeckoLib cache: " + fieldName, exception);
      }
   }

   private static final class LegacyResourceReloadListener
      implements SimpleSynchronousResourceReloadListener, IdentifiableResourceReloadListener {
      @Override
      public ResourceLocation getFabricId() {
         return LISTENER_ID;
      }

      @Override
      public List<ResourceLocation> getFabricDependencies() {
         // must re-run after geckolib's own loader, or its wholesale map swap wipes these entries again
         return List.of(GECKOLIB_LISTENER_ID);
      }

      @Override
      public void onResourceManagerReload(ResourceManager resourceManager) {
         GirlAnimationLengths.reloadFromResourceManager(resourceManager);

         com.google.gson.Gson modelGson = new GsonBuilder()
            .registerTypeAdapter(Model.class, Model.deserializer())
            .create();
         com.google.gson.Gson animationGson = new GsonBuilder()
            .registerTypeAdapter(BakedAnimations.class, new BakedAnimationsAdapter())
            .create();
         final com.google.gson.Gson MODEL_GSON = modelGson;

         Map<ResourceLocation, BakedGeoModel> models = new LinkedHashMap<>();
         Map<ResourceLocation, BakedAnimations> animations = new LinkedHashMap<>();

         for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(LEGACY_MODEL_ROOT, LegacyGeckoLibResourceCompat::isJsonFile).entrySet()) {
            ResourceLocation cacheId = normalizeLegacyModelId(entry.getKey());
            if (cacheId == null) {
               continue;
            }

            try {
               Model rawModel;
               try (java.io.Reader reader = entry.getValue().openAsReader()) {
                  rawModel = modelGson.fromJson(reader, Model.class);
               }
               BakedGeoModel model = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(rawModel));
               models.put(cacheId, model);

               for (String aliasPath : LEGACY_MODEL_ALIASES.getOrDefault(cacheId.getPath(), List.of())) {
                  models.putIfAbsent(cacheId.withPath(aliasPath), model);
               }
            } catch (Exception exception) {
               SexmodMod.LOGGER.warn("Failed to load legacy model resource {}", entry.getKey(), exception);
            }
         }

         for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(LEGACY_ANIMATION_ROOT, LegacyGeckoLibResourceCompat::isJsonFile).entrySet()) {
            ResourceLocation cacheId = normalizeLegacyAnimationId(entry.getKey());
            if (cacheId == null) {
               continue;
            }

            try {
               BakedAnimations bakedAnimations;
               try (java.io.Reader reader = entry.getValue().openAsReader()) {
                  bakedAnimations = animationGson.fromJson(reader, BakedAnimations.class);
               }
               animations.put(cacheId, bakedAnimations);
            } catch (Exception exception) {
               SexmodMod.LOGGER.warn("Failed to load legacy animation resource {}", entry.getKey(), exception);
            }
         }

         if (models.isEmpty() && animations.isEmpty()) {
            return;
         }

         Map<ResourceLocation, BakedGeoModel> mergedModels = new LinkedHashMap<>(GeckoLibCache.getBakedModels());
         mergedModels.putAll(models);
         Map<ResourceLocation, BakedAnimations> mergedAnimations = new LinkedHashMap<>(GeckoLibCache.getBakedAnimations());
         animations.forEach((cacheId, packed) -> mergedAnimations.merge(cacheId, packed, LegacyResourceReloadListener::overlay));

         setGeckoLibCache("MODELS", mergedModels);
         setGeckoLibCache("ANIMATIONS", mergedAnimations);

         SexmodMod.LOGGER.info(
            "Loaded legacy resource-pack compatibility entries: {} models, {} animations",
            models.size(),
            animations.size()
         );
      }

      /**
       * A pack's take on an animation wins, but animations it does not ship keep ours. 1.12.2 packs
       * predate scenes we added, so replacing the file wholesale would leave those animations
       * missing and crash the renderer when a scene asks for one.
       */
      private static BakedAnimations overlay(BakedAnimations ours, BakedAnimations fromPack) {
         Map<String, software.bernie.geckolib.animation.Animation> merged = new LinkedHashMap<>(ours.animations());
         merged.putAll(fromPack.animations());
         return new BakedAnimations(merged);
      }
   }
}
