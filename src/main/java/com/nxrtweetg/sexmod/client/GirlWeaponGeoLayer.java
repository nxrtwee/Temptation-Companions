package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.base.PerBoneRender;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the equipped sword or bow on the {@code weapon} bone. As in the original the weapon only
 * appears while she is actually fighting ({@code GirlRenderer.a}, gated on the {@code Fighter.M}
 * data parameter), so an equipped sword stays out of sight until she swings it.
 */
@Environment(EnvType.CLIENT)
public final class GirlWeaponGeoLayer extends GeoRenderLayer<SceneGirlEntity, Void, LivingEntityRenderState> {
   private static final String BONE = "weapon";

   private final ItemModelResolver itemModelResolver;

   public GirlWeaponGeoLayer(Context context, GeoRenderer<SceneGirlEntity, Void, LivingEntityRenderState> renderer) {
      super(renderer);
      this.itemModelResolver = context.getItemModelResolver();
   }

   @Override
   public void addRenderData(SceneGirlEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
      ItemStack weapon = weaponFor(animatable);
      if (weapon.isEmpty()) {
         return;
      }

      ItemStackRenderState stackState = new ItemStackRenderState();
      this.itemModelResolver
         .updateForTopItem(stackState, weapon, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, animatable.level(), animatable, 0);
      renderState.addGeckolibData(GirlRenderData.WEAPON_STACK_STATE, stackState);
   }

   @Override
   public void addPerBoneRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<LivingEntityRenderState>> consumer) {
      if (!renderPassInfo.willRender()) {
         return;
      }

      LivingEntityRenderState renderState = renderPassInfo.renderState();
      ItemStackRenderState stackState = renderState.getGeckolibData(GirlRenderData.WEAPON_STACK_STATE);
      if (stackState == null) {
         return;
      }

      renderPassInfo.model()
         .getBone(BONE)
         .ifPresent(bone -> consumer.accept(bone, (pass, renderedBone, renderTasks) -> {
            PoseStack poseStack = pass.poseStack();
            poseStack.pushPose();
            stackState.submit(poseStack, renderTasks, pass.packedLight(), OverlayTexture.NO_OVERLAY, renderState.outlineColor);
            poseStack.popPose();
         }));
   }

   private static ItemStack weaponFor(SceneGirlEntity jenny) {
      return switch (jenny.resolvedActivity()) {
         case COMBAT_MELEE -> jenny.getItemBySlot(EquipmentSlot.MAINHAND);
         case COMBAT_BOW -> jenny.getItemBySlot(EquipmentSlot.OFFHAND);
         default -> ItemStack.EMPTY;
      };
   }
}
