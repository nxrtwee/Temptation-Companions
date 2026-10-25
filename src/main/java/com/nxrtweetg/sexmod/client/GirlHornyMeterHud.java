package com.nxrtweetg.sexmod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws the arousal meter and its prompt buttons, recreating {@code ds_class200} of the 1.12.2
 * original. All coordinates below are the original's raw values; it drew the meter under a 0.75
 * GlStateManager scale and the buttons under 0.35, which is why both live in the top-left corner.
 */
@Environment(EnvType.CLIENT)
public final class GirlHornyMeterHud {
   private static final ResourceLocation METER = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/gui/hornymeter.png");
   private static final ResourceLocation BUTTONS = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/gui/buttons.png");
   private static final int TEX = 256;
   private static final float METER_SCALE = 0.75F;
   private static final float BUTTON_SCALE = 0.35F;

   private GirlHornyMeterHud() {
   }

   static void render(GuiGraphics context, DeltaTracker tickCounter) {
      if (!GirlHornyMeter.isVisible()) {
         GirlHornyMeter.resetAnimation();
         return;
      }

      Minecraft minecraft = Minecraft.getInstance();
      float deltaTicks = tickCounter.getGameTimeDeltaTicks();
      float slide = Math.min(1.0F, GirlHornyMeter.slideIn() + deltaTicks / 25.0F);
      GirlHornyMeter.setSlideIn(slide);

      double fill = Mth.lerp(Math.min(1.0, deltaTicks * 0.2), GirlHornyMeter.displayedFill(), GirlHornyMeter.fill());
      GirlHornyMeter.setDisplayedFill(fill);
      boolean finishing = GirlHornyMeter.isFinishing();

      int frameY = (int)meter(Mth.lerp(slide, -200.0F, 10.0F));
      context.blit(METER,
         (int)meter(10.0F), frameY, (int)meterSpan(10.0, 146.0), (int)meterSpan(10.0, 175.0),
         0.0F, 0.0F, 146, 175, TEX, TEX
      );

      if (finishing) {
         float rise = Math.min(1.0F, GirlHornyMeter.finishSlide() + deltaTicks / 15.0F);
         GirlHornyMeter.setFinishSlide(rise);
         double barY = Mth.lerp(rise, 18.0F, -300.0F);
         context.blit(METER,
            (int)meter(67.0F),
            (int)meter(barY),
            (int)meterSpan(67.0, 32.0),
            (int)meterSpan(barY, 160.0),
            159.0F,
            8.0F,
            32,
            160,
            TEX,
            TEX
         );
      } else {
         int barHeight = (int)Mth.lerp(fill, 0.0, 160.0);
         if (barHeight > 0) {
            double barY = Mth.lerp(slide, -45.0F, (float)Mth.lerp(fill, 178.0, 18.0));
            context.blit(METER,
               (int)meter(67.0F),
               (int)meter(barY),
               (int)meterSpan(67.0, 32.0),
               (int)meterSpan(barY, barHeight),
               159.0F,
               (float)Mth.lerp(fill, 167.0, 8.0),
               32,
               barHeight,
               TEX,
               TEX
            );
         }

         int capHeight = (int)Mth.lerp(1.0 - fill, 1.0, 29.0);
         double capY = Mth.lerp(slide, -58.0F, (float)Mth.lerp(1.0 - fill, 178.0, 149.0));
         float capV = (float)Mth.lerp(1.0 - fill, 169.0, 141.0);

         for (double capX : new double[]{18.0, 120.0}) {
            context.blit(METER,
               (int)meter(capX),
               (int)meter(capY),
               (int)meterSpan(capX, 28.0),
               (int)meterSpan(capY, capHeight),
               212.0F,
               capV,
               28,
               capHeight,
               TEX,
               TEX
            );
         }
      }

      if (GirlHornyMeter.showThrustButton() && !finishing) {
         float v = minecraft.options.keyShift.isDown() ? 54.0F : 0.0F;
         double thrustX = Mth.lerp(slide, -200.0F, 98.0F);
         context.blit(BUTTONS,
            (int)button(thrustX),
            (int)button(405.0F),
            (int)buttonSpan(thrustX, 158.0),
            (int)buttonSpan(405.0, 54.0),
            0.0F,
            v,
            158,
            54,
            TEX,
            TEX
         );
      }

      if (GirlHornyMeter.isFull() && !finishing) {
         float v = 108.0F + (minecraft.options.keyJump.isDown() ? 54.0F : 0.0F);
         context.blit(BUTTONS,
            (int)button(240.0F),
            (int)button(160.0F),
            (int)buttonSpan(240.0, 256.0),
            (int)buttonSpan(160.0, 52.0),
            0.0F,
            v,
            256,
            52,
            TEX,
            TEX
         );
      }
   }

   private static int meter(double value) {
      return (int)Math.round(value * METER_SCALE);
   }

   /**
    * Width/height must be derived from the rounded edges, not rounded on their own: scaling each
    * value independently leaves a one-pixel seam between the fill and its frame.
    */
   private static int meterSpan(double origin, double size) {
      return (int)meter(origin + size) - (int)meter(origin);
   }

   private static int button(double value) {
      return (int)Math.round(value * BUTTON_SCALE);
   }

   private static int buttonSpan(double origin, double size) {
      return button(origin + size) - button(origin);
   }
}
