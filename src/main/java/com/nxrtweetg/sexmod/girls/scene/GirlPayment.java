package com.nxrtweetg.sexmod.girls.scene;

import com.nxrtweetg.sexmod.girls.state.GirlPaymentItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

/**
 * Item payment for a scene, as in the 1.12.2 original: a single stack has to cover the whole price
 * ({@code m_class414.actionPerformed}), and the server shrinks exactly one matching stack
 * ({@code t_class423}). Creative players and horny-potion scenes never reach here.
 */
public final class GirlPayment {
   private GirlPayment() {
   }

   public static boolean canAfford(Player player, GirlSceneAction action) {
      return canAfford(player, new ItemStack(action.paymentItem().item(), action.paymentAmount()));
   }

   public static boolean canAfford(Player player, ItemStack price) {
      return player.getAbilities().instabuild || findPayingSlot(player, price) >= 0;
   }

   /** Takes the price out of the player's inventory. Returns false if it could not be paid. */
   public static boolean charge(Player player, GirlSceneAction action) {
      return charge(player, new ItemStack(action.paymentItem().item(), action.paymentAmount()));
   }

   /** Takes the price out of the player's inventory. Returns false if it could not be paid. */
   public static boolean charge(Player player, ItemStack price) {
      if (player.getAbilities().instabuild) {
         return true;
      }

      int slot = findPayingSlot(player, price);
      if (slot < 0) {
         return false;
      }

      player.getInventory().removeItem(slot, price.getCount());
      return true;
   }

   private static int findPayingSlot(Player player, GirlSceneAction action) {
      return findPayingSlot(player, new ItemStack(action.paymentItem().item(), action.paymentAmount()));
   }

   private static int findPayingSlot(Player player, ItemStack price) {
      if (price.isEmpty()) {
         return 0;
      }

      Inventory inventory = player.getInventory();

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (stack.is(price.getItem()) && stack.getCount() >= price.getCount()) {
            return slot;
         }
      }

      return -1;
   }
}
