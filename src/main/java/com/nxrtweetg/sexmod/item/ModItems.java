package com.nxrtweetg.sexmod.item;

import com.nxrtweetg.sexmod.SexmodMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class ModItems {
   private ModItems() {
   }

   private static Item.Properties properties(String id) {
      Identifier identifier = Identifier.fromNamespaceAndPath("sexmod", id);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, identifier);
      return new Item.Properties().setId(key).useItemDescriptionPrefix();
   }

   public static void register() {
      SexmodMod.LOGGER.info("Summon items are currently disabled");
   }
}
