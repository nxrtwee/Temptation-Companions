package com.nxrtweetg.sexmod.girls.state;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum GirlPaymentItem {
   DIAMOND(Items.DIAMOND),
   GOLD(Items.GOLD_INGOT),
   EMERALD(Items.EMERALD),
   ENDER_PEARL(Items.ENDER_PEARL),
   COD(Items.COD),
   SALMON(Items.SALMON);

   private final Item item;

   GirlPaymentItem(Item item) {
      this.item = item;
   }

   public Item item() {
      return this.item;
   }
}
