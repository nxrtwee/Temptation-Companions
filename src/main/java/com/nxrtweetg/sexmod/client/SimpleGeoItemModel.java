package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

@Environment(EnvType.CLIENT)
public class SimpleGeoItemModel<T extends Item & GeoAnimatable> extends GeoModel<T> {
   private final Identifier model;
   private final Identifier texture;
   private final Identifier animation;

   public SimpleGeoItemModel(Identifier model, Identifier texture, Identifier animation) {
      this.model = model;
      this.texture = texture;
      this.animation = animation;
   }

   @Override
   public Identifier getModelResource(GeoRenderState renderState) {
      return this.model;
   }

   @Override
   public Identifier getTextureResource(GeoRenderState renderState) {
      return this.texture;
   }

   @Override
   public Identifier getAnimationResource(T animatable) {
      return this.animation;
   }
}
