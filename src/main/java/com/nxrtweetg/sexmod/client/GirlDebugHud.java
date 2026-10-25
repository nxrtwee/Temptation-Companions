package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public final class GirlDebugHud {
   private static final int COLOR = -1;
   private static final double SEARCH_RADIUS = 12.0;

   private GirlDebugHud() {
   }

   public static void register() {
      HudRenderCallback.EVENT.register(GirlDebugHud::render);
   }

   private static void render(GuiGraphics context, DeltaTracker tickCounter) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.level != null && !minecraft.options.hideGui) {
         Optional<SceneGirlEntity> optional = findNearestGirl(minecraft.player);
         if (!optional.isEmpty()) {
            SceneGirlEntity girl = optional.get();
            int i = 10;
            int j = 10;
            context.drawString(minecraft.font, Component.literal("Girl Debug"), i, j, -1, true);
            context.drawString(minecraft.font, Component.literal("Busy: " + girl.isBusyTracked()), i, j + 12, -1, false);
            context.drawString(
               minecraft.font, Component.literal("Action: " + girl.trackedActiveAction().map(Enum::name).orElse("NONE")), i, j + 24, -1, false
            );
            context.drawString(minecraft.font, Component.literal("Follow-up: " + girl.trackedFollowUpAction().name()), i, j + 36, -1, false);
            context.drawString(
               minecraft.font,
               Component.literal("Payment: " + girl.trackedPaymentAmount() + "x " + girl.trackedPaymentItem().name()),
               i,
               j + 48,
               -1,
               false
            );
            context.drawString(
               minecraft.font,
               Component.literal("Owner: " + girl.trackedActivePlayer().map(uuid -> uuid.toString().substring(0, 8)).orElse("NONE")),
               i,
               j + 60,
               -1,
               false
            );
            context.drawString(
               minecraft.font,
               Component.literal("Freecam: " + (GirlSceneFreecam.isActive() ? "ON [G]" : "OFF [G]") + "  Radius 30"),
               i,
               j + 72,
               -1,
               false
            );
            context.drawString(
               minecraft.font,
               Component.literal(String.format("Speed XZ: %.2f  [[/]]", GirlSceneFreecam.horizontalSpeed())),
               i,
               j + 84,
               -1,
               false
            );
            context.drawString(
               minecraft.font,
               Component.literal(String.format("Speed Y: %.2f  [; / ']", GirlSceneFreecam.verticalSpeed())),
               i,
               j + 96,
               -1,
               false
            );
            context.drawString(
               minecraft.font,
               Component.literal(String.format(
                  "FADE out=%.0f hold=%.0f in=%.0f  [KP4/6  KP2/8  KP1/3]  KP5=preview",
                  GirlSceneTransitionFade.fadeOutTicks(),
                  GirlSceneTransitionFade.blackHoldTicks(),
                  GirlSceneTransitionFade.fadeInTicks()
               )),
               i,
               j + 108,
               0xFFFFAA00,
               false
            );
         }
      }
   }

   private static Optional<SceneGirlEntity> findNearestGirl(Player player) {
      return player.level()
         .getEntitiesOfClass(SceneGirlEntity.class, player.getBoundingBox().inflate(12.0), entity -> entity.isAlive())
         .stream()
         .min(Comparator.comparingDouble(player::distanceToSqr));
   }
}
