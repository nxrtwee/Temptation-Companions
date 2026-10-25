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
      // Geo slots in the original's index order: 0 = base/nude, 1 = the dressed or armored body. Luna's
      // two slots really are one file in the original (cg.java) and our pair was split out of it; the
      // slime and Allie use their armored rig as the everyday body, because only it carries the armour,
      // hand and prop bones our render path needs - see GirlModelSlotPolicy.
      registerRenderer(ModEntities.JENNY, "jenny", "jennynude", "jennydressed");
      registerRenderer(ModEntities.ELLIE, "ellie", "nude", "dressed");
      registerRenderer(ModEntities.BIA, "bia", "bianude", "biadressed");
      registerRenderer(ModEntities.LUNA, "cat", "nude", "dressed");
      registerRenderer(ModEntities.SLIME, "slime", "nude", "dressed");
      registerRenderer(ModEntities.ALLIE, "allie", "allie", "armored");
      registerRenderer(ModEntities.BEE, "bee", "bee", "armored");
      registerRenderer(ModEntities.KOBOLD, "kobold", GirlModelSlotPolicy.ARMORED_WHEN_EQUIPPED, "kobold", "armored");
      registerRenderer(ModEntities.GOBLIN, "goblin", GirlModelSlotPolicy.ARMORED_WHEN_EQUIPPED, "goblin", "armored");
      registerRenderer(ModEntities.GALATH, "galath", "galath", "galath");
      registerRenderer(ModEntities.MANGLELIE, "manglelie", GirlModelSlotPolicy.MANGLELIE_POLICY, "manglelie", "manglelie", "galath/galath_con_mang");
      registerRenderer(ModEntities.CUSTOM_GIRL, "custom", "partner");
      MenuScreens.register(GirlMenuTypes.GIRL_INVENTORY, GirlInventoryScreen::new);
      UseEntityCallback.EVENT.register((UseEntityCallback)(player, world, hand, entity, hitResult) -> {
         if (world.isClientSide()
            && hand == InteractionHand.MAIN_HAND
            && entity instanceof SceneGirlEntity girl
            && girl.hasInteractionMenu()
            && girl.trackedScenePhase() == com.nxrtweetg.sexmod.girls.scene.GirlScenePhase.IDLE
            && !girl.defersInteractionToServer(player.getItemInHand(hand))
            && !GirlSceneInputController.shouldBlockGameplayInput()) {
            Minecraft.getInstance().setScreen(new GirlInteractionScreen(girl));
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
      GirlCustomMenuClientTracker.register();
   }

   private static void registerRenderer(EntityType<? extends SceneGirlEntity> type, String character, String... modelSlots) {
      registerRenderer(type, character, GirlModelSlotPolicy.DIRECT, modelSlots);
   }

   private static void registerRenderer(
      EntityType<? extends SceneGirlEntity> type, String character, GirlModelSlotPolicy slotPolicy, String... modelSlots
   ) {
      if (type == null) {
         return;
      }

      EntityRendererRegistry.register(type, context -> new GirlEntityRenderer(context, new GirlGeoModel(character, slotPolicy, modelSlots)));
   }
}
