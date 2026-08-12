package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.ModEntities;
import com.nxrtweetg.sexmod.girls.inventory.GirlMenuTypes;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

@Environment(EnvType.CLIENT)
public final class GirlClientBootstrap {
   private GirlClientBootstrap() {
   }

   public static void initialize() {
      GirlSceneCameraConfig.load();
      registerRenderer(ModEntities.JENNY, "jenny", "jennydressed", "jennynude");
      registerRenderer(ModEntities.ELLIE, "ellie", "dressed", "nude");
      registerRenderer(ModEntities.BIA, "bia", "biadressed", "bianude");
      registerRenderer(ModEntities.LUNA, "cat", "dressed", "nude");
      registerRenderer(ModEntities.SLIME, "slime", "dressed", "nude");
      registerRenderer(ModEntities.ALLIE, "allie", "armored", "allie");
      registerRenderer(ModEntities.BEE, "bee", "armored", "bee");
      MenuScreens.register(GirlMenuTypes.GIRL_INVENTORY, GirlInventoryScreen::new);
      UseEntityCallback.EVENT.register((UseEntityCallback)(player, world, hand, entity, hitResult) -> {
         if (world.isClientSide() && hand == InteractionHand.MAIN_HAND && entity instanceof SceneGirlEntity girl && !GirlSceneInputController.shouldBlockGameplayInput()) {
            Minecraft.getInstance().gui.setScreen(new GirlInteractionScreen(girl));
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      });
      GirlSceneHud.register();
      // GirlDebugHud.register();  // Debug HUD hidden — numpad fade editor still active via GirlSceneInputController
      GirlSceneInputController.register();
      GirlSceneCameraController.register();
      GirlSceneCameraTuner.register();
      AllieSummonClientTracker.register();
   }

   private static void registerRenderer(EntityType<? extends SceneGirlEntity> type, String character, String dressed, String nude) {
      if (type == null) {
         return;
      }

      EntityRendererRegistry.register(type, context -> new GirlEntityRenderer(context, new GirlGeoModel(character, dressed, nude)));
   }
}
