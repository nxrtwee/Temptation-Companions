package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.inventory.GirlArmorMaterial;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * How a character turns her synced model variant into one of her geo slots - see the history in
 * git for the original's three-slot lookup rules; GeckoLib 4 hands the model the animatable, so
 * the policies read the entity directly instead of a render state.
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface GirlModelSlotPolicy {
   GirlModelSlotPolicy DIRECT = (entity, variant, slotCount) -> variant >= 0 && variant < slotCount ? variant : 0;

   /**
    * Selects slot 1 (armored) only when armor is equipped and variant != 0.
    * When unarmored or stripped, falls back to slot 0 (base nude/unarmored body).
    */
   GirlModelSlotPolicy ARMORED_WHEN_EQUIPPED = (entity, variant, slotCount) -> {
      if (variant == 0 || slotCount < 2) {
         return 0;
      }
      boolean hasArmor = GirlArmorMaterial.indexFor(entity.getItemBySlot(EquipmentSlot.HEAD)) >= 0
         || GirlArmorMaterial.indexFor(entity.getItemBySlot(EquipmentSlot.CHEST)) >= 0
         || GirlArmorMaterial.indexFor(entity.getItemBySlot(EquipmentSlot.LEGS)) >= 0
         || GirlArmorMaterial.indexFor(entity.getItemBySlot(EquipmentSlot.FEET)) >= 0;
      return hasArmor ? 1 : 0;
   };

   GirlModelSlotPolicy MANGLELIE_POLICY = (entity, variant, slotCount) -> 0;

   int resolve(SceneGirlEntity entity, int variant, int slotCount);
}
