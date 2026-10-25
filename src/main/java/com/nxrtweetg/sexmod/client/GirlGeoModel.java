package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
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
   private GirlEntityRenderer rigListener;

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
   public ResourceLocation getModelResource(GeoRenderState renderState) {
      return this.modelSlots[this.resolveSlot(renderState)];
   }

   @Override
   public ResourceLocation getTextureResource(GeoRenderState renderState) {
      ResourceLocation customSkin = renderState.getGeckolibData(GirlRenderData.CUSTOM_SKIN);
      if (customSkin != null) {
         return customSkin;
      }

      if ("custom".equals(this.character)) {
         return CustomSkinManager.DEFAULT_SKIN;
      }

      if ("goblin".equals(this.character)) {
         return this.resolveSlot(renderState) == 0 ? GOBLIN_TEXTURE : GOBLIN_ARMORED_TEXTURE;
      }

      return this.texture;
   }

   /** Never out of range: every policy is handed the slot count and the base one clamps. */
   private int resolveSlot(GeoRenderState renderState) {
      int variant = renderState.getOrDefaultGeckolibData(GirlRenderData.MODEL_VARIANT, 1);
      int slot = this.slotPolicy.resolve(renderState, variant, this.modelSlots.length);
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

   /** Set by the owning renderer so the post-animation rig pass can reach its camera-anchor state. */
   public void setRigListener(GirlEntityRenderer renderer) {
      this.rigListener = renderer;
   }

   /**
    * GeckoLib 5.2's post-animation hook: runs at the end of every animation pass, after the processor
    * has written the final bone transforms and before the renderer walks the bones. Everything the rig
    * does to bones has to happen here - an adjustPositionForRender edit would be overwritten by the
    * animation's absolute bone writes, which run later in the same frame on this GeckoLib line.
    */
   @Override
   public void setCustomAnimations(AnimationState<SceneGirlEntity> animationState) {
      if (this.rigListener != null) {
         GirlEntityRenderer.GirlGeoRenderState renderState = (GirlEntityRenderer.GirlGeoRenderState)animationState.renderState();
         BakedGeoModel model = this.getBakedModel(this.getModelResource(renderState));
         this.rigListener.applyRigPass(model, animationState);
      }
   }
}
