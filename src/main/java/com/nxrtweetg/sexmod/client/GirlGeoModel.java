package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.model.GeoModel;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

/**
 * The asset set for one girl. Her own animation file is searched first and Jenny's is the fallback,
 * which GeckoLib applies per animation name rather than per file, so a girl who lacks a scene still
 * plays it off Jenny's rig.
 *
 * <p>Her body is one of an ordered list of geo slots, given in the original's order: slot 0 is the
 * base or nude model, slot 1 the armored one, slot 2 - for the three characters that have it - the
 * dressed one. Which slot the synced variant selects is {@link GirlModelSlotPolicy}'s business.
 */
@Environment(EnvType.CLIENT)
public final class GirlGeoModel extends GeoModel<SceneGirlEntity> {
   private static final ResourceLocation[] ALL_ANIMATION_FALLBACKS = {
      ResourceLocation.fromNamespaceAndPath("sexmod", "jenny/jenny"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "ellie/ellie"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "bia/bia"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "slime/slime"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "bee/bee"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "allie/allie"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "goblin/goblin"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "kobold/kobold"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "galath/galath"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "manglelie/manglelie"),
      ResourceLocation.fromNamespaceAndPath("sexmod", "cat/cat")
   };
   private static final ResourceLocation GOBLIN_TEXTURE = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/entity/goblin/goblin.png");
   private static final ResourceLocation GOBLIN_ARMORED_TEXTURE = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/entity/goblin/goblinbk.png");

   private final ResourceLocation[] modelSlots;
   private final GirlModelSlotPolicy slotPolicy;
   private final ResourceLocation texture;
   private final ResourceLocation animations;
   private final ResourceLocation[] animationFallbacks;
   private final String character;

   /** The common case: two slots read straight off the variant. */
   public GirlGeoModel(String character, String... modelNames) {
      this(character, GirlModelSlotPolicy.DIRECT, modelNames);
   }

   public GirlGeoModel(String character, GirlModelSlotPolicy slotPolicy, String... modelNames) {
      if (modelNames.length == 0) {
         throw new IllegalArgumentException("Girl " + character + " needs at least one geo slot");
      }

      this.character = character;
      this.slotPolicy = slotPolicy;
      this.modelSlots = new ResourceLocation[modelNames.length];

      for (int slot = 0; slot < modelNames.length; slot++) {
         String name = modelNames[slot];
         this.modelSlots[slot] = name.contains("/")
            ? ResourceLocation.fromNamespaceAndPath("sexmod", name)
            : ResourceLocation.fromNamespaceAndPath("sexmod", character + "/" + name);
      }

      this.texture = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/entity/" + character + "/" + character + ".png");
      this.animations = ResourceLocation.fromNamespaceAndPath("sexmod", character + "/" + character);
      this.animationFallbacks = java.util.Arrays.stream(ALL_ANIMATION_FALLBACKS)
         .filter(id -> !id.equals(this.animations))
         .toArray(ResourceLocation[]::new);
   }

   @Override
   public ResourceLocation getModelResource(SceneGirlEntity animatable) {
      return this.modelSlots[this.resolveSlot(animatable)];
   }

   @Override
   public ResourceLocation getTextureResource(SceneGirlEntity animatable) {
      ResourceLocation customSkin = animatable instanceof com.nxrtweetg.sexmod.girls.custom.CustomGirlEntity customGirl && customGirl.getCustomData() != null
            ? CustomSkinManager.getOrLoadSkin(customGirl.getCustomData().id(), customGirl.getCustomData().skinFileName())
            : null;
      if (customSkin != null) {
         return customSkin;
      }

      if ("custom".equals(this.character)) {
         return CustomSkinManager.DEFAULT_SKIN;
      }

      if ("goblin".equals(this.character)) {
         return this.resolveSlot(animatable) == 0 ? GOBLIN_TEXTURE : GOBLIN_ARMORED_TEXTURE;
      }

      return this.texture;
   }

   /** Never out of range: every policy is handed the slot count and the base one clamps. */
   private int resolveSlot(SceneGirlEntity animatable) {
      int variant = animatable.trackedModelVariant();
      if (variant != 0 && variant != 1) variant = animatable.coreState().currentModel();
      int slot = this.slotPolicy.resolve(animatable, variant, this.modelSlots.length);
      return slot >= 0 && slot < this.modelSlots.length ? slot : 0;
   }

   @Override
   public ResourceLocation getAnimationResource(SceneGirlEntity animatable) {
      return this.animations;
   }

   @Override
   public ResourceLocation[] getAnimationResourceFallbacks(SceneGirlEntity animatable) {
      return this.animationFallbacks;
   }
}
