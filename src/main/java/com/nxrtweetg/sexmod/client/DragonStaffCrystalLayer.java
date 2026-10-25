package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nxrtweetg.sexmod.item.DragonStaffItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class DragonStaffCrystalLayer extends GeoRenderLayer<DragonStaffItem> {
   private static final ResourceLocation END_CRYSTAL_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/end_crystal/end_crystal.png");
   private ModelPart crystalRoot;

   public DragonStaffCrystalLayer(GeoRenderer<DragonStaffItem> renderer) {
      super(renderer);
   }

   @Override
   public void render(
      PoseStack poseStack, DragonStaffItem animatable, BakedGeoModel model,
      RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
      float partialTick, int packedLight, int packedOverlay
   ) {
      GeoBone energyBone = model.getBone("energy").orElse(null);
      if (energyBone == null) {
         energyBone = model.getBone("staff").orElse(null);
      }
      if (energyBone == null) {
         return;
      }

      if (this.crystalRoot == null) {
         this.crystalRoot = EndCrystalRenderer.createBodyLayer().bakeRoot();
      }

      float animTicks;
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         animTicks = mc.player.tickCount + mc.getTimer().getGameTimeDeltaPartialTick(false);
      } else {
         animTicks = (System.currentTimeMillis() % 100000L) / 50.0F;
      }

      poseStack.pushPose();
      RenderUtil.prepMatrixForBone(poseStack, energyBone);

      // If attached to "staff" rather than "energy", shift up to staff head (Y=22.5 units = ~1.406m)
      if ("staff".equals(energyBone.getName())) {
         poseStack.translate(0.0F, 1.406F, 0.0F);
      }

      // Floating / bobbing and breathing motion authentic to donor fa_class300
      float bob = (float) Math.sin(animTicks * 0.1F) * 0.03F;
      poseStack.translate(0.0F, bob, 0.0F);

      // Authentic scale from donor (~0.35 total)
      poseStack.scale(0.35F, 0.35F, 0.35F);

      // 1.21.1 has no crystal render state - the spin vanilla derived from it goes on the parts
      float spin = animTicks * 3.0F;
      ModelPart glass = this.crystalRoot.getChild("glass");
      ModelPart cube = this.crystalRoot.getChild("cube");
      glass.yRot = spin * 0.05F;
      cube.yRot = -spin * 0.1F;

      VertexConsumer crystalBuffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION));
      glass.render(poseStack, crystalBuffer, packedLight, OverlayTexture.NO_OVERLAY, -1);
      cube.render(poseStack, crystalBuffer, packedLight, OverlayTexture.NO_OVERLAY, -1);

      poseStack.popPose();
   }
}
