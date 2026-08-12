package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.allie.AllieEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class AllieSummonClientTracker {
   private static int pendingTicks;

   private AllieSummonClientTracker() {
   }

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         if (pendingTicks <= 0 || client.player == null || client.level == null) {
            return;
         }

         pendingTicks--;
         AllieEntity nearest = null;
         double best = 16.0;

         for (AllieEntity candidate : client.level.getEntitiesOfClass(AllieEntity.class, client.player.getBoundingBox().inflate(4.0))) {
            double distance = candidate.distanceToSqr(client.player);
            if (distance < best) {
               best = distance;
               nearest = candidate;
            }
         }

         if (nearest != null) {
            Minecraft.getInstance().gui.setScreen(new GirlInteractionScreen(nearest));
            pendingTicks = 0;
         }
      });
   }

   public static void queueOpen() {
      pendingTicks = 30;
   }
}
