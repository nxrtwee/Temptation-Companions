package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlCustomPhase;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

/**
 * Opens the follow-up menu when a custom interaction starts waiting on an answer - Bia's talk ends
 * that way. The ordinary path cannot do it: {@code UseEntityCallback} is dead while she owns the
 * player, and the server has no way to push a screen.
 */
@Environment(EnvType.CLIENT)
public final class GirlCustomMenuClientTracker {
   /** The girl we last opened the menu for, so a menu the player closed himself stays closed. */
   private static int promptedEntityId = -1;

   private GirlCustomMenuClientTracker() {
   }

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         if (client.player == null || client.level == null) {
            promptedEntityId = -1;
            return;
         }

         Optional<SceneGirlEntity> optional = GirlSceneInputController.findOwnedGirl(client.player);
         if (optional.isEmpty() || optional.get().customPhase() != GirlCustomPhase.WAITING) {
            // the wait is over (she answered, or the interaction ended): arm the prompt again
            promptedEntityId = -1;
            return;
         }

         SceneGirlEntity girl = optional.get();
         if (promptedEntityId == girl.getId()) {
            return;
         }

         if (client.gui.screen() instanceof GirlInteractionScreen) {
            // it is already up: count it as the prompt so closing it does not bring it back
            promptedEntityId = girl.getId();
         } else if (client.gui.screen() == null) {
            promptedEntityId = girl.getId();
            Minecraft.getInstance().gui.setScreen(new GirlInteractionScreen(girl));
         }
         // any other screen (the tuning panel, a container): leave it alone and try again once it closes
      });
   }
}
