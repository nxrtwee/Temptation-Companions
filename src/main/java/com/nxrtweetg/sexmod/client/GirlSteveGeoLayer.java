package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * Draws the partner's body (steve subtree) in passes:
 *
 * <p><b>Jenny</b>: single pass with JENNY_ATLAS covering the full subtree including genitals
 * (all UVs repacked into her atlas by the original mod). Player skin is not used.
 *
 * <p><b>All other characters</b>: two passes —
 * <ol>
 *   <li>Player's real skin — full steve subtree, genital bones hidden.
 *   <li>Hardcoded {@code steve.png} — only genital bones. {@code steve.png} has flesh-coloured
 *       pixels at the UV coords the degenerate genital cubes sample.
 * </ol>
 */
@Environment(EnvType.CLIENT)
public final class GirlSteveGeoLayer extends GeoRenderLayer<SceneGirlEntity, Void, LivingEntityRenderState> {
   private static final String BONE_STEVE = "steve";

   private static final Identifier JENNY_ATLAS = Identifier.fromNamespaceAndPath("sexmod", "textures/entity/jenny/jenny.png");
   private static final Identifier PLAYER_SKIN = Identifier.fromNamespaceAndPath("sexmod", "textures/player/steve.png");

   /** Genital bones — hidden from player-skin pass, shown only in the genital pass. */
   private static final String[] GENITAL_BONES = {"cock", "ballL", "ballR", "bone123", "bone99"};

   /**
    * Body bones hidden in the genital pass (self + all children).
    * boyCam is child of Head2 — covered by Head2's skipChildrenRender.
    * Torso2 is handled separately via setMeshOnly (mesh hidden, children kept traversable).
    * "blocks" is a child of Torso2 carrying the player torso cubes in Galath and Manglelie rigs.
    */
   private static final String[] BODY_BONES_TO_HIDE = {"Head2", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "blocks"};

   public GirlSteveGeoLayer(GeoRenderer<SceneGirlEntity, Void, LivingEntityRenderState> renderer) {
      super(renderer);
   }

   @Override
   protected Identifier getTextureResource(LivingEntityRenderState renderState) {
      // All characters (including jenny after UV repack) use standard player skin layout.
      Identifier playerSkin = renderState.getGeckolibData(GirlRenderData.PARTNER_SKIN);
      return playerSkin != null ? playerSkin : PLAYER_SKIN;
   }

   @Override
   public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
      LivingEntityRenderState renderState = renderPassInfo.renderState();
      if (!renderPassInfo.willRender() || !renderState.getOrDefaultGeckolibData(GirlRenderData.RENDER_PLAYER, false)) {
         return;
      }

      GeoBone steve = renderPassInfo.model().getBone(BONE_STEVE).orElse(null);
      if (steve == null) {
         return;
      }

      int packedLight   = renderPassInfo.packedLight();
      int packedOverlay = renderPassInfo.packedOverlay();
      int renderColor   = renderPassInfo.renderColor();

      boolean isJenny = "jenny".equals(renderState.getOrDefaultGeckolibData(GirlRenderData.CHARACTER, "jenny"));

      // Pass 1: player skin — full body, genital bones hidden.
      // All characters now share the same UV layout (jenny geo was repacked to match standard skin).
      RenderType skinType = this.renderer.getRenderType(renderState, this.getTextureResource(renderState));
      if (skinType != null) {
         renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), skinType, (pose, vc) -> {
            PoseStack ps = renderPassInfo.poseStack();
            ps.pushPose();
            ps.last().set(pose);
            renderPassInfo.renderPosed(() -> {
               setBonesHidden(renderPassInfo, GENITAL_BONES, true);
               boolean partnerIsSlim = renderState.getOrDefaultGeckolibData(GirlRenderData.PARTNER_IS_SLIM, false);
               setBonesHidden(renderPassInfo, partnerIsSlim ? GirlEntityRenderer.STEVE_ARM_BONES : GirlEntityRenderer.ALEX_ARM_BONES, true);
               setBonesHidden(renderPassInfo, partnerIsSlim ? GirlEntityRenderer.ALEX_ARM_BONES : GirlEntityRenderer.STEVE_ARM_BONES, false);
               renderBone(renderPassInfo, steve, vc, packedLight, packedOverlay, renderColor);
               setBonesHidden(renderPassInfo, GENITAL_BONES, false);
            });
            ps.popPose();
         });
      }

      // Pass 2: steve.png — only genitals (all characters including jenny).
      RenderType steveType = this.renderer.getRenderType(renderState, PLAYER_SKIN);
      if (steveType != null) {
         renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), steveType, (pose, vc) -> {
            PoseStack ps = renderPassInfo.poseStack();
            ps.pushPose();
            ps.last().set(pose);
            renderPassInfo.renderPosed(() -> {
               setBonesHidden(renderPassInfo, GENITAL_BONES, false);
               setMeshOnly(renderPassInfo, "Torso2", true);
               setBonesHidden(renderPassInfo, BODY_BONES_TO_HIDE, true);
               renderBone(renderPassInfo, steve, vc, packedLight, packedOverlay, renderColor);
               setBonesHidden(renderPassInfo, BODY_BONES_TO_HIDE, false);
               setMeshOnly(renderPassInfo, "Torso2", false);
               setBonesHidden(renderPassInfo, GENITAL_BONES, true);
            });
            ps.popPose();
         });
      }
   }

   /**
    * Hides/un-hides a set of bones by name.
    * GeckoLib treats frameSnapshot==null as "always render" — if a bone was never visited in
    * adjustModelBonesForRender its snapshot is null and flag-writes are silently lost.
    * We inject a temporary snapshot when hiding so the flag takes effect, and remove it on
    * restore only if the snapshot carries no meaningful transform (i.e. we created it).
    */
   private static void setBonesHidden(RenderPassInfo<LivingEntityRenderState> pass, String[] names, boolean hidden) {
      for (String name : names) {
         pass.model().getBone(name).ifPresent(b -> setBoneHidden(b, hidden));
      }
   }

   private static void setBoneHidden(GeoBone b, boolean hidden) {
      if (hidden) {
         if (b.frameSnapshot == null) {
            BoneSnapshot.create(b).apply();
         }
         b.frameSnapshot.skipRender(true).skipChildrenRender(true);
      } else {
         if (b.frameSnapshot == null) return;
         b.frameSnapshot.skipRender(false).skipChildrenRender(false);
         BoneSnapshot s = b.frameSnapshot;
         if (!s.hasTranslation() && !s.hasRotation() && !s.hasScale()) {
            s.cleanup(); // was a temporary snapshot we injected; remove it
         }
      }
   }

   /** Hides only the mesh of a bone, leaving its children traversable (for Torso2). */
   private static void setMeshOnly(RenderPassInfo<LivingEntityRenderState> pass, String name, boolean hidden) {
      pass.model().getBone(name).ifPresent(b -> {
         if (hidden) {
            if (b.frameSnapshot == null) {
               BoneSnapshot.create(b).apply();
            }
            b.frameSnapshot.skipRender(true);
         } else {
            if (b.frameSnapshot == null) return;
            b.frameSnapshot.skipRender(false);
            BoneSnapshot s = b.frameSnapshot;
            if (!s.hasTranslation() && !s.hasRotation() && !s.hasScale()) {
               s.cleanup();
            }
         }
      });
   }

   private static void renderBone(
      RenderPassInfo<LivingEntityRenderState> pass,
      GeoBone bone,
      VertexConsumer vc,
      int packedLight,
      int packedOverlay,
      int renderColor
   ) {
      BoneSnapshot snap      = bone.frameSnapshot;
      boolean wasHidden      = snap != null && snap.isHidden();
      boolean wasChildHidden = snap != null && snap.areChildrenHidden();
      if (snap != null) snap.skipRender(false).skipChildrenRender(false);
      try {
         bone.positionAndRender(pass, vc, packedLight, packedOverlay, renderColor);
      } finally {
         if (snap != null) snap.skipRender(wasHidden).skipChildrenRender(wasChildHidden);
      }
   }
}
