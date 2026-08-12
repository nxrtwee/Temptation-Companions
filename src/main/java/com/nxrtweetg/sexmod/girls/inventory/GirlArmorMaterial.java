package com.nxrtweetg.sexmod.girls.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

/**
 * The four armour looks baked into jenny.png. The original picked one by shifting the armour UVs at
 * render time (GirlRenderer: +72 texels per material); GeckoLib 5 has no such hook, so the model
 * carries four copies of every armour bone and the renderer shows one set.
 *
 * <p>Ordinals must match the bone name suffixes in jennydressed.geo.json.
 */
public enum GirlArmorMaterial {
   DIAMOND("diamond"),
   GOLD("gold"),
   IRON("iron"),
   LEATHER("leather");

   private final String suffix;

   GirlArmorMaterial(String suffix) {
      this.suffix = suffix;
   }

   public String suffix() {
      return this.suffix;
   }

   /** Index of the material to draw for this stack, or -1 when nothing should be drawn. */
   public static int indexFor(ItemStack stack) {
      GirlArmorMaterial material = of(stack);
      return material == null ? -1 : material.ordinal();
   }

   public static GirlArmorMaterial of(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      }

      Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
      if (equippable == null || equippable.assetId().isEmpty()) {
         return null;
      }

      var asset = equippable.assetId().get();
      if (asset == EquipmentAssets.LEATHER) {
         return LEATHER;
      } else if (asset == EquipmentAssets.GOLD) {
         return GOLD;
      } else if (asset == EquipmentAssets.DIAMOND) {
         return DIAMOND;
      } else {
         // iron/chainmail/copper/netherite/scute all fall back to the plain metal look rather than
         // rendering nothing, which is what the original did for chainmail too
         return IRON;
      }
   }
}
