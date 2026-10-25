package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import com.nxrtweetg.sexmod.item.AllieLampItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class AllieLampRenderer extends GeoItemRenderer<AllieLampItem> {
   private static final String[] ARM_BONES = {"rightArm", "leftArm"};

   public AllieLampRenderer() {
      super(new SimpleGeoItemModel<>(
         Identifier.fromNamespaceAndPath("sexmod", "allie/lamp"),
         Identifier.fromNamespaceAndPath("sexmod", "textures/entity/allie/lamp.png"),
         Identifier.fromNamespaceAndPath("sexmod", "allie/lamp")
      ));
   }

   @Override
   public void preRenderPass(RenderPassInfo<GeoRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
      super.preRenderPass(renderPassInfo, renderTasks);
      // Severed arm bones from the Blockbench model are permanently hidden across all perspectives.
      // The lamp is held cleanly in the player's real hand without fake arms or looping inspection animations.
      for (String boneName : ARM_BONES) {
         renderPassInfo.model().getBone(boneName).ifPresent(AllieLampRenderer::hideBone);
      }
   }

   static void hideBone(GeoBone b) {
      if (b.frameSnapshot == null) {
         BoneSnapshot.create(b).apply();
      }
      b.frameSnapshot.skipRender(true).skipChildrenRender(true);
   }
}
