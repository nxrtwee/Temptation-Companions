package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@Environment(EnvType.CLIENT)
public class SimpleGeoItemModel<T extends Item & GeoAnimatable> extends GeoModel<T> {
   private final ResourceLocation model;
   private final ResourceLocation texture;
   private final ResourceLocation animation;

   public SimpleGeoItemModel(ResourceLocation model, ResourceLocation texture, ResourceLocation animation) {
      this.model = model;
      this.texture = texture;
      this.animation = animation;
   }

   @Override
   public ResourceLocation getModelResource(T animatable, GeoRenderer<T> renderer) {
      return this.model;
   }

   @Override
   public ResourceLocation getTextureResource(T animatable, GeoRenderer<T> renderer) {
      return this.texture;
   }

   @Override
   public ResourceLocation getAnimationResource(T animatable) {
      return this.animation;
   }
}
