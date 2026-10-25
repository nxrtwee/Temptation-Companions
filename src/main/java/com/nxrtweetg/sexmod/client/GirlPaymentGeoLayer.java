package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.PerBoneRender;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.state.GirlPaymentItem;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
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
public final class GirlPaymentGeoLayer extends GeoRenderLayer<SceneGirlEntity, Void, LivingEntityRenderState> {
   private static final String BONE = "itemRenderer";
   /** The original rendered the payment at 1:1 bone scale ({@code GirlEntity.n} defaults to 1.0f). */
   private static final float SCALE = 1.0F;

   private final ItemModelResolver itemModelResolver;

   public GirlPaymentGeoLayer(Context context, GeoRenderer<SceneGirlEntity, Void, LivingEntityRenderState> renderer) {
      super(renderer);
      this.itemModelResolver = context.getItemModelResolver();
   }

   @Override
   public void addRenderData(SceneGirlEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
      int amount = animatable.trackedPaymentAmount();
      if (amount <= 0) {
         return;
      }

      GirlPaymentItem payment = animatable.trackedPaymentItem();
      ItemStackRenderState stackState = new ItemStackRenderState();
      this.itemModelResolver
         .updateForTopItem(stackState, new ItemStack(payment.item()), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, animatable.level(), animatable, 0);
      renderState.addGeckolibData(GirlRenderData.PAYMENT_STACK_STATE, stackState);
   }

   @Override
   public void addPerBoneRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<LivingEntityRenderState>> consumer) {
      if (!renderPassInfo.willRender()) {
         return;
      }

      LivingEntityRenderState renderState = renderPassInfo.renderState();
      ItemStackRenderState stackState = renderState.getGeckolibData(GirlRenderData.PAYMENT_STACK_STATE);
      int amount = renderState.getOrDefaultGeckolibData(GirlRenderData.PAYMENT_AMOUNT, 0);
      if (stackState == null || amount <= 0) {
         return;
      }

      renderPassInfo.model()
         .getBone(BONE)
         .ifPresent(bone -> consumer.accept(bone, (pass, renderedBone, renderTasks) -> {
            PoseStack poseStack = pass.poseStack();

            for (int i = 0; i < amount; i++) {
               poseStack.pushPose();
               // fan the coins apart so a stack of three reads as three items, as in the original
               poseStack.translate(0.0F, 0.0F, (i - (amount - 1) / 2.0F) * 0.025F);
               poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((i - (amount - 1) / 2.0F) * -15.0F));
               poseStack.scale(SCALE, SCALE, SCALE);
               stackState.submit(poseStack, renderTasks, pass.packedLight(), OverlayTexture.NO_OVERLAY, renderState.outlineColor);
               poseStack.popPose();
            }
         }));
   }
}
