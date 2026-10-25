package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the partner's body (steve subtree) in passes:
 *
 * <p><b>Jenny</b>: single pass with JENNY_ATLAS covering the full subtree including genitals
 * (all UVs repacked into her atlas by the original mod). Player skin is not used.
 *
 * <p><b>All other characters</b>: two passes -
 * <ol>
 *   <li>Player's real skin - full steve subtree, genital bones hidden.</li>
 *   <li>Hardcoded {@code steve.png} - only genital bones. {@code steve.png} has flesh-coloured
 *       pixels at the UV coords the degenerate genital cubes sample.</li>
 * </ol>
 */
@Environment(EnvType.CLIENT)
public final class GirlSteveGeoLayer extends GeoRenderLayer<SceneGirlEntity, Void, GirlEntityRenderer.GirlGeoRenderState> {
   private static final String BONE_STEVE = "steve";

   private static final ResourceLocation JENNY_ATLAS = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/entity/jenny/jenny.png");
   private static final ResourceLocation PLAYER_SKIN = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/player/steve.png");

   /** Genital bones - hidden from player-skin pass, shown only in the genital pass. */
   private static final String[] GENITAL_BONES = {"cock", "ballL", "ballR", "bone123", "bone99"};

   /**
    * Body bones hidden in the genital pass (self + all children).
    * boyCam is child of Head2 - covered by Head2's child hiding.
    * Torso2 is handled separately via a mesh-only hide (cubes skipped, children still walked).
    */
   private static final String[] BODY_BONES_TO_HIDE = {"Head2", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "blocks"};

   public GirlSteveGeoLayer(GeoRenderer<SceneGirlEntity, Void, GirlEntityRenderer.GirlGeoRenderState> renderer) {
      super(renderer);
   }

   private ResourceLocation resolveTexture(GirlEntityRenderer.GirlGeoRenderState renderState) {
      // All characters (including jenny after UV repack) use standard player skin layout.
      ResourceLocation playerSkin = renderState.getGeckolibData(GirlRenderData.PARTNER_SKIN);
      return playerSkin != null ? playerSkin : PLAYER_SKIN;
   }

   @Override
   public void render(
      GirlEntityRenderer.GirlGeoRenderState renderState, PoseStack poseStack, BakedGeoModel model,
      RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
      int packedLight, int packedOverlay, int renderColor
   ) {
      if (!renderState.getOrDefaultGeckolibData(GirlRenderData.RENDER_PLAYER, false)) {
         return;
      }

      GeoBone steve = model.getBone(BONE_STEVE).orElse(null);
      if (steve == null) {
         return;
      }

      boolean partnerIsSlim = renderState.getOrDefaultGeckolibData(GirlRenderData.PARTNER_IS_SLIM, false);

      // Pass 1: player skin - full body, genital bones hidden and the wrong arm variant hidden.
      RenderType skinType = this.renderer.getRenderType(renderState, this.resolveTexture(renderState));
      if (skinType != null) {
         VertexConsumer skinBuffer = bufferSource.getBuffer(skinType);
         setBonesHidden(model, GENITAL_BONES, true);
         GirlEntityRenderer.applyPartnerArmModel(model, partnerIsSlim);
         renderSubtree(renderState, poseStack, steve, bufferSource, skinBuffer, packedLight, packedOverlay, renderColor);
         setBonesHidden(model, GENITAL_BONES, false);
      }

      // Pass 2: steve.png - only genitals (all characters including jenny).
      RenderType steveType = this.renderer.getRenderType(renderState, PLAYER_SKIN);
      if (steveType != null) {
         VertexConsumer steveBuffer = bufferSource.getBuffer(steveType);
         setBonesHidden(model, GENITAL_BONES, false);
         setMeshOnly(model, "Torso2", true);
         setBonesHidden(model, BODY_BONES_TO_HIDE, true);
         renderSubtree(renderState, poseStack, steve, bufferSource, steveBuffer, packedLight, packedOverlay, renderColor);
         setBonesHidden(model, BODY_BONES_TO_HIDE, false);
         setMeshOnly(model, "Torso2", false);
         setBonesHidden(model, GENITAL_BONES, true);
      }
   }

   /**
    * GeckoLib 5.2's own recursion skips a hidden bone together with its children, which the mesh-only
    * hide of Torso2 cannot express - so the subtree is walked by hand: cubes of visible bones,
    * children of bones that are not hiding them.
    */
   private void renderSubtree(
      GirlEntityRenderer.GirlGeoRenderState renderState, PoseStack poseStack, GeoBone bone,
      MultiBufferSource bufferSource, VertexConsumer buffer,
      int packedLight, int packedOverlay, int renderColor
   ) {
      if (!bone.isHidden()) {
         poseStack.pushPose();
         RenderUtil.prepMatrixForBone(poseStack, bone);
         this.renderer.renderCubesOfBone(renderState, bone, poseStack, buffer, packedLight, packedOverlay, renderColor);
         poseStack.popPose();
      }

      if (!bone.isHidingChildren()) {
         for (GeoBone child : bone.getChildBones()) {
            renderSubtree(renderState, poseStack, child, bufferSource, buffer, packedLight, packedOverlay, renderColor);
         }
      }
   }

   private static void setBonesHidden(BakedGeoModel model, String[] names, boolean hidden) {
      for (String name : names) {
         model.getBone(name).ifPresent(bone -> {
            bone.setHidden(hidden);
            bone.setChildrenHidden(hidden);
         });
      }
   }

   /** Hides only the mesh of a bone, leaving its children traversable (for Torso2). */
   private static void setMeshOnly(BakedGeoModel model, String name, boolean hidden) {
      model.getBone(name).ifPresent(bone -> bone.setHidden(hidden));
   }
}
