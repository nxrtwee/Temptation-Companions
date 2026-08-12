package com.nxrtweetg.sexmod.girls.inventory;

import com.nxrtweetg.sexmod.girls.GirlEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Girl equipment screen: six filtered slots in a row above the player's inventory.
 *
 * <p>Coordinates are measured off girlinventory.png. The original (d4_class162) placed its slots
 * relative to the screen while drawing the panel at a +52 offset, so its raw numbers do not line up
 * with the background on their own.
 */
public final class GirlInventoryMenu extends AbstractContainerMenu {
   /** Per-slot, not a fixed stride: the weapon/bow pair sits apart from the four armour slots. */
   private static final int[] EQUIPMENT_X = {31, 50, 72, 91, 110, 129};
   private static final int EQUIPMENT_Y = 8;
   private static final int PLAYER_INVENTORY_X = 8;
   private static final int PLAYER_INVENTORY_Y = 32;
   private static final int HOTBAR_Y = 90;
   private static final double MAX_USE_DISTANCE = 12.0;

   private final Container equipment;
   private final GirlEntity girl;

   public GirlInventoryMenu(int containerId, Inventory playerInventory, GirlEntity girl) {
      this(containerId, playerInventory, girl, girl.equipmentContainer());
   }

   /** Client side: the entity may not be resolvable, so an empty backing container is used. */
   public GirlInventoryMenu(int containerId, Inventory playerInventory, int entityId) {
      this(
         containerId,
         playerInventory,
         playerInventory.player.level().getEntity(entityId) instanceof GirlEntity girl ? girl : null,
         playerInventory.player.level().getEntity(entityId) instanceof GirlEntity girl
            ? girl.equipmentContainer()
            : new SimpleContainer(GirlEquipmentSlot.Kind.COUNT)
      );
   }

   private GirlInventoryMenu(int containerId, Inventory playerInventory, GirlEntity girl, Container equipment) {
      super(GirlMenuTypes.GIRL_INVENTORY, containerId);
      this.girl = girl;
      this.equipment = equipment;
      checkContainerSize(equipment, GirlEquipmentSlot.Kind.COUNT);
      equipment.startOpen(playerInventory.player);

      for (GirlEquipmentSlot.Kind kind : GirlEquipmentSlot.Kind.values()) {
         this.addSlot(new GirlEquipmentSlot(equipment, kind, EQUIPMENT_X[kind.ordinal()], EQUIPMENT_Y));
      }

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(
               new Slot(playerInventory, column + row * 9 + 9, PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18)
            );
         }
      }

      for (int column = 0; column < 9; column++) {
         this.addSlot(new Slot(playerInventory, column, PLAYER_INVENTORY_X + column * 18, HOTBAR_Y));
      }
   }

   @Override
   public ItemStack quickMoveStack(Player player, int slotIndex) {
      Slot slot = this.slots.get(slotIndex);
      if (slot == null || !slot.hasItem()) {
         return ItemStack.EMPTY;
      }

      ItemStack slotStack = slot.getItem();
      ItemStack original = slotStack.copy();
      int equipmentSize = GirlEquipmentSlot.Kind.COUNT;

      if (slotIndex < equipmentSize) {
         if (!this.moveItemStackTo(slotStack, equipmentSize, this.slots.size(), true)) {
            return ItemStack.EMPTY;
         }
      } else if (!this.moveItemStackTo(slotStack, 0, equipmentSize, false)) {
         return ItemStack.EMPTY;
      }

      if (slotStack.isEmpty()) {
         slot.setByPlayer(ItemStack.EMPTY);
      } else {
         slot.setChanged();
      }

      return original;
   }

   @Override
   public void removed(Player player) {
      super.removed(player);
      this.equipment.stopOpen(player);
   }

   @Override
   public boolean stillValid(Player player) {
      return this.girl != null && this.girl.isAlive() && this.girl.distanceToSqr(player) <= MAX_USE_DISTANCE * MAX_USE_DISTANCE;
   }
}
