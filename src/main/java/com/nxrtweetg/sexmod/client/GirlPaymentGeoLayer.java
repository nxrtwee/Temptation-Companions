package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
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
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the offered payment as a real item on the {@code itemRenderer} bone, the way the 1.12.2
 * original did ({@code GirlRenderer.b}).
 */
@Environment(EnvType.CLIENT)
public final class GirlPaymentGeoLayer extends GeoRenderLayer<SceneGirlEntity> {
   private static final String BONE = "itemRenderer";
   /** The original rendered the payment at 1:1 bone scale ({@code GirlEntity.n} defaults to 1.0f). */
   private static final float SCALE = 1.0F;

   private final ItemModelResolver itemModelResolver;

   public GirlPaymentGeoLayer(Context context, GeoRenderer<SceneGirlEntity> renderer) {
      super(renderer);
      this.itemModelResolver = context.getItemModelResolver();
   }

   @Override
   public void render(
      PoseStack poseStack, SceneGirlEntity animatable, BakedGeoModel model,
      RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
      float partialTick, int packedLight, int packedOverlay, int renderColor
   ) {
      int amount = animatable.trackedPaymentAmount();
      if (amount <= 0) {
         return;
      }

      GirlPaymentItem payment = animatable.trackedPaymentItem();
      GeoBone bone = model.getBone(BONE).orElse(null);
      if (bone == null) {
         return;
      }

      ItemStackRenderState stackState = new ItemStackRenderState();
      this.itemModelResolver.updateForTopItem(
         stackState, new ItemStack(payment.item()), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, Minecraft.getInstance().level, animatable, 0
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
