package com.nxrtweetg.sexmod.client;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nxrtweetg.sexmod.item.DragonStaffItem;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
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

   @Override
   public void addPerBoneRender(RenderPassInfo<GeoRenderState> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<GeoRenderState>> biConsumer) {
      BakedGeoModel bakedModel = this.getDefaultBakedModel(renderPassInfo.renderState());
      if (bakedModel == null) {
         return;
      }
      GeoBone energyBone = bakedModel.getBone("energy").orElse(null);
      if (energyBone == null) {
         energyBone = bakedModel.getBone("staff").orElse(null);
      }
      if (energyBone != null) {
         biConsumer.accept(energyBone, this::renderCrystal);
      }
   }

   private void renderCrystal(RenderPassInfo<GeoRenderState> info, GeoBone bone, SubmitNodeCollector collector) {
      if (this.crystalModel == null) {
         this.crystalModel = new EndCrystalModel(EndCrystalModel.createBodyLayer().bakeRoot());
      }
      PoseStack poseStack = info.poseStack();
      poseStack.pushPose();

      float animTicks = 0.0F;
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.getDeltaTracker() != null) {
         animTicks = mc.player.tickCount + mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
      } else {
         animTicks = (System.currentTimeMillis() % 100000L) / 50.0F;
      }

      // If attached to "staff" rather than "energy", shift up to staff head (Y=22.5 units = ~1.406m)
      if ("staff".equals(bone.name())) {
         poseStack.translate(0.0F, 1.406F, 0.0F);
      }

      // Floating / bobbing and breathing motion authentic to donor fa_class300
      float bob = (float) Math.sin(animTicks * 0.1F) * 0.03F;
      poseStack.translate(0.0F, bob, 0.0F);

      // Authentic scale from donor (~0.35 total)
      poseStack.scale(0.35F, 0.35F, 0.35F);

      this.renderState.showsBottom = false;
      this.renderState.ageInTicks = animTicks;
      this.renderState.lightCoords = info.packedLight();

      this.crystalModel.outerGlass.resetPose();
      this.crystalModel.innerGlass.resetPose();
      this.crystalModel.cube.resetPose();
      this.crystalModel.setupAnim(this.renderState);

      collector.submitModel(
         this.crystalModel,
         this.renderState,
         poseStack,
         END_CRYSTAL_LOCATION,
         info.packedLight(),
         OverlayTexture.NO_OVERLAY,
         -1
      );

      poseStack.popPose();
   }
}
