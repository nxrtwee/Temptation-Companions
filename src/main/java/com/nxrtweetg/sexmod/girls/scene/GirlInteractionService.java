package com.nxrtweetg.sexmod.girls.scene;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class GirlInteractionService {
   private GirlInteractionService() {
   }

   public static Optional<SceneGirlEntity> findNearestGirl(ServerPlayer player, double radius) {
      ServerLevel serverlevel = player.level();
      // one who has no menu is not a candidate: she would otherwise shadow the girl the command meant
      List<SceneGirlEntity> list = serverlevel.getEntitiesOfClass(
         SceneGirlEntity.class, player.getBoundingBox().inflate(radius), entity -> entity.isAlive() && entity.hasInteractionMenu()
      );
      return list.stream().min(Comparator.comparingDouble(player::distanceToSqr));
   }

   public static boolean tryBeginInteraction(ServerPlayer player, GirlSceneAction action) {
      Optional<SceneGirlEntity> optional = findNearestGirl(player, 8.0);
      if (optional.isEmpty()) {
         player.displayClientMessage(Component.literal("No girl found nearby."), false);
         return false;
      } else {
         return tryBeginInteraction(player, optional.get(), action);
      }
   }

   public static boolean tryBeginInteraction(ServerPlayer player, SceneGirlEntity girl, GirlSceneAction action) {
      if (!action.enabled()) {
         player.displayClientMessage(Component.literal("That scene is not available."), false);
         return false;
      }

      // A girl who sells nothing cannot be asked for anything: the original gives her no interaction
      // handler at all, and her scenes start from her own tick.
      if (!girl.hasInteractionMenu()) {
         player.displayClientMessage(Component.literal(girl.getName().getString() + " does not deal with you."), false);
         return false;
      }

      if (!canInteract(player, girl)) {
         return false;
      }

      String name = girl.getName().getString();
      Optional<GirlBedTable.Variant> variant = girl.requiresBed(action) ? GirlBedTable.variantFor(action) : Optional.empty();
      if (variant.isPresent()) {
         Optional<GirlBedTable.Anchor> anchor = GirlBedTable.find(girl, variant.get());
         if (anchor.isEmpty()) {
            player.displayClientMessage(
               Component.literal("<" + name + "> ").append(Component.translatable(girl.dialogueKey(variant.get().failureDialogueKey()))),
               false
            );
            return false;
         }

         girl.setSceneBedAnchor(anchor.get());
      } else {
         girl.clearSceneBedAnchor();
      }

      int paymentAmount = girl.hasPaymentPhase(action) ? girl.paymentAmountFor(action) : 0;
      if (paymentAmount > 0 && !GirlPayment.charge(player, new net.minecraft.world.item.ItemStack(girl.paymentItemFor(action).item(), paymentAmount))) {
         player.displayClientMessage(Component.literal("<" + name + "> you cannot afford that..."), false);
         return false;
      }

      girl.beginInteraction(player, action);
      player.displayClientMessage(Component.literal("Started " + name + " action: " + action.legacyId()), false);
      return true;
   }

   public static boolean canInteract(ServerPlayer player, SceneGirlEntity girl) {
      String name = girl.getName().getString();
      if (!girl.isAlive()) {
         player.displayClientMessage(Component.literal(name + " is no longer available."), false);
         return false;
      }

      if (girl.coreState().activePlayerId() != null && player.level().getPlayerByUUID(girl.coreState().activePlayerId()) == null) {
         girl.resetScene();
      }

      if (girl.coreState().activePlayerId() != null && !girl.coreState().activePlayerId().equals(player.getUUID())) {
         player.displayClientMessage(Component.literal(name + " is already busy."), false);
         return false;
      }

      if (girl.coreState().activePlayerId() != null && girl.coreState().activePlayerId().equals(player.getUUID()) && girl.hasActiveScene()) {
         player.displayClientMessage(Component.literal("Finish or reset the current scene before starting another one."), false);
         return false;
      }

      if (player.distanceToSqr(girl) > 64.0) {
         player.displayClientMessage(Component.literal("You are too far away from " + name + "."), false);
         return false;
      }
      return true;
   }
}
