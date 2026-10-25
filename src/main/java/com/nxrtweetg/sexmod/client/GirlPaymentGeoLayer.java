package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.state.GirlPaymentItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the offered payment as a real item on the {@code itemRenderer} bone, the way the 1.12.2
 * original did ({@code GirlRenderer.b}). The 1.21.4 port instead baked one cube set per currency,
 * which capped prices at diamond/gold/emerald.
 */
@Environment(EnvType.CLIENT)
public final class GirlPaymentGeoLayer extends GeoRenderLayer<SceneGirlEntity, Void, GirlEntityRenderer.GirlGeoRenderState> {
   private static final String BONE = "itemRenderer";
   /** The original rendered the payment at 1:1 bone scale ({@code GirlEntity.n} defaults to 1.0f). */
   private static final float SCALE = 1.0F;

   private final ItemModelResolver itemModelResolver;

   public GirlPaymentGeoLayer(Context context, GeoRenderer<SceneGirlEntity, Void, GirlEntityRenderer.GirlGeoRenderState> renderer) {
      super(renderer);
      this.itemModelResolver = context.getItemModelResolver();
   }

   @Override
   public void render(
      GirlEntityRenderer.GirlGeoRenderState renderState, PoseStack poseStack, BakedGeoModel model,
      RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
      int packedLight, int packedOverlay, int renderColor
   ) {
      int amount = renderState.getOrDefaultGeckolibData(GirlRenderData.PAYMENT_AMOUNT, 0);
      if (amount <= 0) {
         return;
      }

      // The tracked amount stays positive until the whole scene resets, but the offer is only on
      // the table during the payment phase - afterwards the item would keep floating at the bone's
      // parked position (eleven blocks below the model) for the rest of the scene.
      com.nxrtweetg.sexmod.girls.scene.GirlScenePhase phase =
         renderState.getOrDefaultGeckolibData(GirlRenderData.SCENE_PHASE, com.nxrtweetg.sexmod.girls.scene.GirlScenePhase.IDLE);
      if (phase != com.nxrtweetg.sexmod.girls.scene.GirlScenePhase.PAYMENT) {
         return;
      }

      GirlPaymentItem payment = renderState.getOrDefaultGeckolibData(GirlRenderData.PAYMENT_ITEM, GirlPaymentItem.DIAMOND);
      GeoBone bone = model.getBone(BONE).orElse(null);
      if (bone == null) {
         return;
      }

      ItemStackRenderState stackState = new ItemStackRenderState();
      this.itemModelResolver.updateForTopItem(
         stackState, new ItemStack(payment.item()), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, Minecraft.getInstance().level, null, 0
      );

      poseStack.pushPose();
      RenderUtil.prepMatrixForBone(poseStack, bone);
      for (int i = 0; i < amount; i++) {
         poseStack.pushPose();
         // fan the coins apart so a stack of three reads as three items, as in the original
         poseStack.translate(0.0F, 0.0F, (i - (amount - 1) / 2.0F) * 0.025F);
         poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((i - (amount - 1) / 2.0F) * -15.0F));
         poseStack.scale(SCALE, SCALE, SCALE);
         stackState.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
         poseStack.popPose();
      }
      poseStack.popPose();
   }
}
