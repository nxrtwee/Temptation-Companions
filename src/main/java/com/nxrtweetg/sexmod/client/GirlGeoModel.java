package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

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
   private static final Identifier[] ALL_ANIMATION_FALLBACKS = {
      Identifier.fromNamespaceAndPath("sexmod", "jenny/jenny"),
      Identifier.fromNamespaceAndPath("sexmod", "ellie/ellie"),
      Identifier.fromNamespaceAndPath("sexmod", "bia/bia"),
      Identifier.fromNamespaceAndPath("sexmod", "slime/slime"),
      Identifier.fromNamespaceAndPath("sexmod", "bee/bee"),
      Identifier.fromNamespaceAndPath("sexmod", "allie/allie"),
      Identifier.fromNamespaceAndPath("sexmod", "goblin/goblin"),
      Identifier.fromNamespaceAndPath("sexmod", "kobold/kobold"),
      Identifier.fromNamespaceAndPath("sexmod", "galath/galath"),
      Identifier.fromNamespaceAndPath("sexmod", "manglelie/manglelie"),
      Identifier.fromNamespaceAndPath("sexmod", "cat/cat")
   };
   private static final Identifier GOBLIN_TEXTURE = Identifier.fromNamespaceAndPath("sexmod", "textures/entity/goblin/goblin.png");
   private static final Identifier GOBLIN_ARMORED_TEXTURE = Identifier.fromNamespaceAndPath("sexmod", "textures/entity/goblin/goblinbk.png");

   private final Identifier[] modelSlots;
   private final GirlModelSlotPolicy slotPolicy;
   private final Identifier texture;
   private final Identifier animations;
   private final Identifier[] animationFallbacks;
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
      this.modelSlots = new Identifier[modelNames.length];

      for (int slot = 0; slot < modelNames.length; slot++) {
         String name = modelNames[slot];
         this.modelSlots[slot] = name.contains("/")
            ? Identifier.fromNamespaceAndPath("sexmod", name)
            : Identifier.fromNamespaceAndPath("sexmod", character + "/" + name);
      }

      this.texture = Identifier.fromNamespaceAndPath("sexmod", "textures/entity/" + character + "/" + character + ".png");
      this.animations = Identifier.fromNamespaceAndPath("sexmod", character + "/" + character);
      this.animationFallbacks = java.util.Arrays.stream(ALL_ANIMATION_FALLBACKS)
         .filter(id -> !id.equals(this.animations))
         .toArray(Identifier[]::new);
   }

   @Override
   public Identifier getModelResource(GeoRenderState renderState) {
      return this.modelSlots[this.resolveSlot(renderState)];
   }

   @Override
   public Identifier getTextureResource(GeoRenderState renderState) {
      Identifier customSkin = renderState.getGeckolibData(GirlRenderData.CUSTOM_SKIN);
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
   public Identifier getAnimationResource(SceneGirlEntity animatable) {
      return this.animations;
   }

   @Override
   public Identifier[] getAnimationResourceFallbacks(SceneGirlEntity animatable) {
      return this.animationFallbacks;
   }
}
