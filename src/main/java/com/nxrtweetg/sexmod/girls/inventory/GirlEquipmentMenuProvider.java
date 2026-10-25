package com.nxrtweetg.sexmod.girls.inventory;

import com.nxrtweetg.sexmod.girls.GirlEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Sends the girl's entity id along with the screen, so the client binds to the right container. */
public record GirlEquipmentMenuProvider(GirlEntity girl) implements ExtendedScreenHandlerFactory<Integer> {
   @Override
   public Integer getScreenOpeningData(ServerPlayer player) {
      return this.girl.getId();
   }

   @Override
   public Component getDisplayName() {
      return this.girl.getName();
   }

   @Override
   public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
      return new GirlInventoryMenu(containerId, inventory, this.girl);
   }
}
