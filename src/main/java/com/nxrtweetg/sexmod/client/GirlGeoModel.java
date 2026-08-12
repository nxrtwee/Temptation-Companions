package com.nxrtweetg.sexmod.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

/**
 * The asset set for one girl. Her own animation file is searched first and Jenny's is the fallback,
 * which GeckoLib applies per animation name rather than per file, so a girl who lacks a scene still
 * plays it off Jenny's rig.
 */
@Environment(EnvType.CLIENT)
public final class GirlGeoModel extends GeoModel<SceneGirlEntity> {
   private static final Identifier JENNY_ANIMATIONS = Identifier.fromNamespaceAndPath("sexmod", "jenny/jenny");
   private static final Identifier[] NO_FALLBACK = new Identifier[0];
   private static final Identifier[] JENNY_FALLBACK = {JENNY_ANIMATIONS};
   private static final Identifier GOBLIN_TEXTURE = Identifier.fromNamespaceAndPath("sexmod", "textures/entity/goblin/goblin.png");
   private static final Identifier GOBLIN_ARMORED_TEXTURE = Identifier.fromNamespaceAndPath("sexmod", "textures/entity/goblin/goblinbk.png");

   private final Identifier dressedModel;
   private final Identifier nudeModel;
   private final Identifier texture;
   private final Identifier animations;
   private final Identifier[] animationFallbacks;
   private final String character;

   public GirlGeoModel(String character, String dressedModelName, String nudeModelName) {
      this.character = character;
      this.dressedModel = Identifier.fromNamespaceAndPath("sexmod", character + "/" + dressedModelName);
      this.nudeModel = Identifier.fromNamespaceAndPath("sexmod", character + "/" + nudeModelName);
      this.texture = Identifier.fromNamespaceAndPath("sexmod", "textures/entity/" + character + "/" + character + ".png");
      this.animations = Identifier.fromNamespaceAndPath("sexmod", character + "/" + character);
      this.animationFallbacks = this.animations.equals(JENNY_ANIMATIONS) ? NO_FALLBACK : JENNY_FALLBACK;
   }

   @Override
   public Identifier getModelResource(GeoRenderState renderState) {
      return renderState.getOrDefaultGeckolibData(GirlRenderData.MODEL_VARIANT, 1) == 0 ? this.nudeModel : this.dressedModel;
   }

   @Override
   public Identifier getTextureResource(GeoRenderState renderState) {
      if ("goblin".equals(this.character)) {
         return renderState.getOrDefaultGeckolibData(GirlRenderData.MODEL_VARIANT, 1) == 0 ? GOBLIN_TEXTURE : GOBLIN_ARMORED_TEXTURE;
      }

      return this.texture;
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
