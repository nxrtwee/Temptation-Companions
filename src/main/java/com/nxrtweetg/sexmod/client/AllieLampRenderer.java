package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nxrtweetg.sexmod.item.AllieLampItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class AllieLampRenderer extends GeoItemRenderer<AllieLampItem> {
   private static final String[] ARM_BONES = {"rightArm", "leftArm"};

   public AllieLampRenderer() {
      super(new SimpleGeoItemModel<>(
         ResourceLocation.fromNamespaceAndPath("sexmod", "allie/lamp"),
         ResourceLocation.fromNamespaceAndPath("sexmod", "textures/entity/allie/lamp.png"),
         ResourceLocation.fromNamespaceAndPath("sexmod", "allie/lamp")
      ));
   }

   @Override
   public void preRender(
      PoseStack poseStack, AllieLampItem animatable, BakedGeoModel model, MultiBufferSource bufferSource,
      VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor
   ) {
      super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);
      // Severed arm bones from the Blockbench model are permanently hidden across all perspectives.
      // The lamp is held cleanly in the player's real hand without fake arms or looping inspection animations.
      for (String boneName : ARM_BONES) {
         model.getBone(boneName).ifPresent(bone -> {
            bone.setHidden(true);
            bone.setChildrenHidden(true);
         });
      }
   }
}
