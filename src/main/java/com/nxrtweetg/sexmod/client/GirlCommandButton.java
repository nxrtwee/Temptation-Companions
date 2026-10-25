package com.nxrtweetg.sexmod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.renderer.RenderType;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * One of the universal command buttons down the left edge. It starts out as a bare icon and grows
 * its label out to the right on hover, the way the original menu did (m_class414.a: the width array
 * {@code d} steps by 7 towards {@code e[i]}, and the label only appears past 14).
 */
@Environment(EnvType.CLIENT)
public final class GirlCommandButton extends AbstractButton {
   private static final ResourceLocation WIDGETS = ResourceLocation.parse("textures/gui/widgets.png");
   private static final ResourceLocation GIRL_INVENTORY = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/gui/girlinventory.png");
   private static final int TEXTURE_SIZE = 256;
   private static final int ICON_SIZE = 16;
   private static final int EXPANSION_STEP = 7;
   private static final int LABEL_THRESHOLD = 14;
   private static final int COLLAPSED_WIDTH = 23;
   private static final int ICON_MARGIN = 3;
   private static final int LABEL_MARGIN = 4;
   private static final int LABEL_HEIGHT = 8;

   private final int targetX;
   private final int iconU;
   private final int expandedWidth;
   private final Runnable onCommand;
   private int expansion;
   private float slide;

   public GirlCommandButton(int x, int y, int height, Component label, int iconU, int expandedWidth, Runnable onCommand) {
      super(x, y, COLLAPSED_WIDTH, height, label);
      this.targetX = x;
      this.iconU = iconU;
      this.expandedWidth = expandedWidth;
      this.onCommand = onCommand;
      this.visible = false;
   }

   public void setSlide(float slide) {
      this.slide = slide;
   }

   @Override
   public void onPress() {
      this.onCommand.run();
   }

   @Override
   protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
      if (this.slide <= 0.0F || !this.visible || this.getX() + this.getWidth() <= 0) {
         return;
      }

      this.expansion = this.isHovered()
         ? Math.min(this.expandedWidth, this.expansion + EXPANSION_STEP)
         : Math.max(0, this.expansion - EXPANSION_STEP);
      this.setX(Mth.lerpInt(this.slide, -COLLAPSED_WIDTH, this.targetX));
      this.setWidth(COLLAPSED_WIDTH + this.expansion);

      if (this.getWidth() < 8 || this.getX() + this.getWidth() <= 0) {
         return;
      }

      int vOffset = 46 + (this.isActive() ? 0 : 20) + (this.isHoveredOrFocused() ? 20 : 0);
      context.blit(WIDGETS,
         this.getX(), this.getY(), 0, vOffset,
         this.getWidth() / 2, this.getHeight(), 256, 256
      );
      context.blit(WIDGETS,
         this.getX() + this.getWidth() / 2, this.getY(), 200 - this.getWidth() / 2, vOffset,
         this.getWidth() - this.getWidth() / 2, this.getHeight(), 256, 256
      );
      context.blit(GIRL_INVENTORY,
         this.getX() + this.getWidth() - ICON_SIZE - ICON_MARGIN,
         this.getY() + (this.getHeight() - ICON_SIZE) / 2,
         this.iconU,
         0,
         ICON_SIZE,
         ICON_SIZE,
         TEXTURE_SIZE,
         TEXTURE_SIZE
      );

      if (this.expansion > LABEL_THRESHOLD) {
         // Centred over the label area only, not the whole button: extractDefaultLabel would centre
         // across the icon too and run the text underneath it.
         int labelRight = this.getX() + this.getWidth() - ICON_SIZE - ICON_MARGIN;
         if (labelRight > this.getX() + LABEL_MARGIN) {
            context.drawCenteredString(
               Minecraft.getInstance().font,
               this.getMessage(),
               (this.getX() + LABEL_MARGIN + labelRight) / 2,
               this.getY() + (this.getHeight() - LABEL_HEIGHT) / 2,
               16777215
            );
         }
      }
   }

   @Override
   protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
      this.defaultButtonNarrationText(output);
   }
}
