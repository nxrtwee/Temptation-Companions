package com.nxrtweetg.sexmod.girls.inventory;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** One equipment slot of the girl inventory, with the original's per-slot item filter. */
public final class GirlEquipmentSlot extends Slot {
   private final GirlEquipmentSlot.Kind kind;

   public GirlEquipmentSlot(Container container, GirlEquipmentSlot.Kind kind, int x, int y) {
      super(container, kind.ordinal(), x, y);
      this.kind = kind;
   }

   @Override
   public boolean mayPlace(ItemStack itemStack) {
      return accepts(this.kind, itemStack);
   }

   @Override
   public int getMaxStackSize() {
      return 1;
   }

   /** 26.2 dropped SwordItem/DiggerItem, so weapons and tools are matched by tag instead. */
   public static boolean accepts(GirlEquipmentSlot.Kind kind, ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return true;
      }

      return switch (kind) {
         case WEAPON -> itemStack.is(ItemTags.SWORDS)
            || itemStack.is(ItemTags.AXES)
            || itemStack.is(ItemTags.PICKAXES)
            || itemStack.is(ItemTags.SHOVELS)
            || itemStack.is(ItemTags.HOES);
         case BOW -> itemStack.is(Items.BOW) || itemStack.is(Items.CROSSBOW);
         case HELMET -> itemStack.is(ItemTags.HEAD_ARMOR);
         case CHESTPLATE -> itemStack.is(ItemTags.CHEST_ARMOR);
         case LEGGINGS -> itemStack.is(ItemTags.LEG_ARMOR);
         case BOOTS -> itemStack.is(ItemTags.FOOT_ARMOR);
      };
   }

   /** Slot order matches the original's inventory indices (fe_class305.b_inner307). */
   public enum Kind {
      WEAPON,
      BOW,
      HELMET,
      CHESTPLATE,
      LEGGINGS,
      BOOTS;

      public static final int COUNT = values().length;
   }
}
