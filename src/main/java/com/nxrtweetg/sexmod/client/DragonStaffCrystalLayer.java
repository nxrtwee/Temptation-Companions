package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nxrtweetg.sexmod.item.DragonStaffItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class DragonStaffCrystalLayer extends GeoRenderLayer<DragonStaffItem, GeoItemRenderer.RenderData, GeoRenderState> {
   private static final Identifier END_CRYSTAL_LOCATION = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/end_crystal/end_crystal.png");
   private EndCrystalModel crystalModel;
   private final EndCrystalRenderState renderState = new EndCrystalRenderState();

   public DragonStaffCrystalLayer(GeoRenderer<DragonStaffItem, GeoItemRenderer.RenderData, GeoRenderState> renderer) {
      super(renderer);
   }

   /**
    * The crystal rides the ordinary layer hook rather than a per-bone render: GeckoLib walks its
    * per-bone renders from two places on this version, which submitted the crystal twice per
    * frame - one copy coming out untextured.
    */
   @Override
   public void submitRenderTask(RenderPassInfo<GeoRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
      if (!renderPassInfo.willRender()) {
         return;
      }

      BakedGeoModel bakedModel = this.getDefaultBakedModel(renderPassInfo.renderState());
      if (bakedModel == null) {
         return;
      }
      GeoBone energyBone = bakedModel.getBone("energy").orElse(null);
      GeoBone staffBone = bakedModel.getBone("staff").orElse(null);
      final GeoBone crystalBone = energyBone != null ? energyBone : staffBone;
      if (crystalBone == null) {
         return;
      }

      if (this.crystalModel == null) {
         this.crystalModel = new EndCrystalModel(EndCrystalModel.createBodyLayer().bakeRoot());
      }

      Minecraft mc = Minecraft.getInstance();
      final float animTicks = mc.player != null && mc.getDeltaTracker() != null
         ? mc.player.tickCount + mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)
         : (System.currentTimeMillis() % 100000L) / 50.0F;

      this.renderState.showsBottom = false;
      this.renderState.ageInTicks = animTicks;
      this.renderState.lightCoords = renderPassInfo.packedLight();

      this.crystalModel.outerGlass.resetPose();
      this.crystalModel.innerGlass.resetPose();
      this.crystalModel.cube.resetPose();
      this.crystalModel.setupAnim(this.renderState);

      renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), this.crystalModel.renderType(END_CRYSTAL_LOCATION), (pose, vc) -> {
         PoseStack ps = renderPassInfo.poseStack();
         ps.pushPose();
         ps.last().set(pose);
         renderPassInfo.renderPosed(() -> {
            RenderUtil.prepMatrixForBoneAndUpdateListeners(ps, crystalBone, renderPassInfo);
            // prepMatrix ends translated away from the bone's pivot; the crystal is anchored at
            // the pivot itself, so step back onto it before the floating offsets
            crystalBone.translateToPivotPoint(ps);

            // If attached to "staff" rather than "energy", shift up to staff head (Y=22.5 units = ~1.406m)
            if ("staff".equals(crystalBone.name())) {
               ps.translate(0.0F, 1.406F, 0.0F);
            }

            // Floating / bobbing and breathing motion authentic to donor fa_class300
            float bob = (float) Math.sin(animTicks * 0.1F) * 0.03F;
            ps.translate(0.0F, bob, 0.0F);

            // Authentic scale from donor (~0.35 total)
            ps.scale(0.35F, 0.35F, 0.35F);

            this.crystalModel.renderToBuffer(ps, vc, renderPassInfo.packedLight(), OverlayTexture.NO_OVERLAY, -1);
         });
         ps.popPose();
      });
   }
}
