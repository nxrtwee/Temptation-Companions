package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nxrtweetg.sexmod.item.DragonStaffItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndCrystalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class DragonStaffCrystalLayer extends GeoRenderLayer<DragonStaffItem, GeoItemRenderer.RenderData, GeoRenderState> {
   private static final ResourceLocation END_CRYSTAL_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/end_crystal/end_crystal.png");
   private EndCrystalModel crystalModel;
   private final EndCrystalRenderState renderState = new EndCrystalRenderState();

   public DragonStaffCrystalLayer(GeoRenderer<DragonStaffItem, GeoItemRenderer.RenderData, GeoRenderState> renderer) {
      super(renderer);
   }

   @Override
   public void render(
      GeoRenderState renderState, PoseStack poseStack, BakedGeoModel model,
      RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
      int packedLight, int packedOverlay, int renderColor
   ) {
      BakedGeoModel bakedModel = this.getDefaultBakedModel(renderState);
      if (bakedModel == null) {
         return;
      }
      GeoBone energyBone = bakedModel.getBone("energy").orElse(null);
      if (energyBone == null) {
         energyBone = bakedModel.getBone("staff").orElse(null);
      }
      if (energyBone == null) {
         return;
      }

      if (this.crystalModel == null) {
         this.crystalModel = new EndCrystalModel(EndCrystalModel.createBodyLayer().bakeRoot());
      }

      float animTicks = 0.0F;
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.getDeltaTracker() != null) {
         animTicks = mc.player.tickCount + mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
      } else {
         animTicks = (System.currentTimeMillis() % 100000L) / 50.0F;
      }

      poseStack.pushPose();
      RenderUtil.prepMatrixForBone(poseStack, energyBone);
      // prepMatrixForBone leaves the origin in the bone's cube space (translated away from the
      // pivot); anchoring the crystal on the pivot keeps it at the bone's joint instead of one
      // bone length below it.
      RenderUtil.translateToPivotPoint(poseStack, energyBone);

      // If attached to "staff" rather than "energy", shift up to staff head (Y=22.5 units = ~1.406m)
      if ("staff".equals(energyBone.getName())) {
         poseStack.translate(0.0F, 1.406F, 0.0F);
      }

      // Floating / bobbing and breathing motion authentic to donor fa_class300
      float bob = (float) Math.sin(animTicks * 0.1F) * 0.03F;
      poseStack.translate(0.0F, bob, 0.0F);

      // Authentic scale from donor (~0.35 total)
      poseStack.scale(0.35F, 0.35F, 0.35F);

      this.crystalModel.resetPose();
      this.renderState.showsBottom = false;
      this.renderState.ageInTicks = animTicks;
      this.crystalModel.setupAnim(this.renderState);

      VertexConsumer crystalBuffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION));
      this.crystalModel.renderToBuffer(poseStack, crystalBuffer, 0xF000F0, OverlayTexture.NO_OVERLAY, -1);

      poseStack.popPose();
   }
}
