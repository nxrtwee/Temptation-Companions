package com.nxrtweetg.sexmod.girls.inventory;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public final class GirlMenuTypes {
   public static final MenuType<GirlInventoryMenu> GIRL_INVENTORY = Registry.register(
      BuiltInRegistries.MENU,
      Identifier.fromNamespaceAndPath("sexmod", "girl_inventory"),
      new ExtendedMenuType<>(GirlInventoryMenu::new, ByteBufCodecs.VAR_INT.cast())
   );

   private GirlMenuTypes() {
   }

   public static void register() {
   }
}
