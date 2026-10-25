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
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Draws the equipped sword or bow on the {@code weapon} bone. As in the original the weapon only
 * appears while she is actually fighting ({@code GirlRenderer.a}, gated on the {@code Fighter.M}
 * data parameter), so an equipped sword stays out of sight until she swings it.
 *
 * <p>The stack state itself is filled by {@code GirlEntityRenderer#fillRenderState}, which still has
 * the entity at hand; this layer only places it on the bone.
 */
@Environment(EnvType.CLIENT)
public final class GirlWeaponGeoLayer extends GeoRenderLayer<SceneGirlEntity, Void, GirlEntityRenderer.GirlGeoRenderState> {
   private static final String BONE = "weapon";

   public GirlWeaponGeoLayer(Context context, GeoRenderer<SceneGirlEntity, Void, GirlEntityRenderer.GirlGeoRenderState> renderer) {
      super(renderer);
   }

   @Override
   public void render(
      GirlEntityRenderer.GirlGeoRenderState renderState, PoseStack poseStack, BakedGeoModel model,
      RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
      int packedLight, int packedOverlay, int renderColor
   ) {
      ItemStackRenderState stackState = renderState.getGeckolibData(GirlRenderData.WEAPON_STACK_STATE);
      if (stackState == null) {
         return;
      }

      GeoBone bone = model.getBone(BONE).orElse(null);
      if (bone == null) {
         return;
      }

      poseStack.pushPose();
      RenderUtil.prepMatrixForBone(poseStack, bone);
      stackState.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
      poseStack.popPose();
   }
}
