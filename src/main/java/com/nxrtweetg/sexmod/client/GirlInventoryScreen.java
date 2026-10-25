package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.inventory.GirlInventoryMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Equipment screen for the girls, drawn from the original's girlinventory.png atlas. */
@Environment(EnvType.CLIENT)
public final class GirlInventoryScreen extends AbstractContainerScreen<GirlInventoryMenu> {
   private static final ResourceLocation GIRL_INVENTORY = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/gui/girlinventory.png");
   private static final int TEXTURE_SIZE = 256;
   /** The panel sits at this offset inside the atlas (fb_class301). */
   private static final int BACKGROUND_U = 33;
   private static final int BACKGROUND_V = 16;
   private static final int PANEL_WIDTH = 176;
   private static final int PANEL_HEIGHT = 114;

   public GirlInventoryScreen(GirlInventoryMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title);
      this.imageWidth = PANEL_WIDTH;
      this.imageHeight = PANEL_HEIGHT;
   }

   @Override
   protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
      context.blit(
         RenderPipelines.GUI_TEXTURED,
         GIRL_INVENTORY,
         this.leftPos,
         this.topPos,
         BACKGROUND_U,
         BACKGROUND_V,
         PANEL_WIDTH,
         PANEL_HEIGHT,
         TEXTURE_SIZE,
         TEXTURE_SIZE
      );
   }

   @Override
   protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
   }
}
