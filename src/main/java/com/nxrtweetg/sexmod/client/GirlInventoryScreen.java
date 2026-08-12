package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.inventory.GirlInventoryMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Equipment screen for the girls, drawn from the original's girlinventory.png atlas. */
@Environment(EnvType.CLIENT)
public final class GirlInventoryScreen extends AbstractContainerScreen<GirlInventoryMenu> {
   private static final Identifier GIRL_INVENTORY = Identifier.fromNamespaceAndPath("sexmod", "textures/gui/girlinventory.png");
   private static final int TEXTURE_SIZE = 256;
   /** The panel sits at this offset inside the atlas (fb_class301). */
   private static final int BACKGROUND_U = 33;
   private static final int BACKGROUND_V = 16;
   private static final int PANEL_WIDTH = 176;
   private static final int PANEL_HEIGHT = 114;

   public GirlInventoryScreen(GirlInventoryMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title, PANEL_WIDTH, PANEL_HEIGHT);
   }

   @Override
   public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
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
      super.extractContents(context, mouseX, mouseY, delta);
   }

   @Override
   protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
   }
}
