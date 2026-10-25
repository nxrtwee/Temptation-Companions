package com.nxrtweetg.sexmod.client;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the equipped sword or bow on the {@code weapon} bone. As in the original the weapon only
 * appears while she is actually fighting, so an equipped sword stays out of sight until she swings it.
 */
@Environment(EnvType.CLIENT)
public final class GirlWeaponGeoLayer extends GeoRenderLayer<SceneGirlEntity> {
   private static final String BONE = "weapon";

   public GirlWeaponGeoLayer(GeoRenderer<SceneGirlEntity> renderer) {
      super(renderer);
   }

   @Override
   public void render(
      PoseStack poseStack, SceneGirlEntity animatable, BakedGeoModel model,
      RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
      float partialTick, int packedLight, int packedOverlay
   ) {
      ItemStack weapon = weaponFor(animatable);
      if (weapon.isEmpty()) {
         return;
      }

      GeoBone bone = model.getBone(BONE).orElse(null);
      if (bone == null) {
         return;
      }

      poseStack.pushPose();
      RenderUtil.prepMatrixForBone(poseStack, bone);
      Minecraft.getInstance().getItemRenderer().renderStatic(
         animatable, weapon, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false,
         poseStack, bufferSource, animatable.level(), packedLight, OverlayTexture.NO_OVERLAY, 0
      );
      poseStack.popPose();
   }

   /** The weapon she is actually fighting with, mirroring the original's {@code GirlRenderer.a} gate. */
   private static ItemStack weaponFor(SceneGirlEntity girl) {
      return switch (girl.resolvedActivity()) {
         case COMBAT_MELEE -> girl.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
         case COMBAT_BOW -> girl.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND);
         default -> ItemStack.EMPTY;
      };
   }
}
