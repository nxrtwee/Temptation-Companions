package com.nxrtweetg.sexmod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

/**
 * Free-look for scenes.
 *
 * <p>The camera anchor and Jenny's facing are both derived from the player's yaw, so letting the
 * player actually turn swung the whole scene while the framing stayed put. Instead the mouse now
 * feeds this offset: the player entity keeps the yaw the scene pinned it to, and only the camera
 * rotates around it.
 *
 * <p>Yaw is unrestricted: these offsets feed the camera alone, never Jenny's pose or the anchor, so
 * a full turn cannot desync the scene. Pitch keeps the engine's own limit to avoid flipping over.
 */
@Environment(EnvType.CLIENT)
public final class GirlSceneFreeLook {
   private static final float MIN_PITCH = -90.0F;
   private static final float MAX_PITCH = 90.0F;
   private static float yawOffset;
   private static float pitchOffset;

   private GirlSceneFreeLook() {
   }

   public static void applyMouseDelta(double yawDelta, double pitchDelta) {
      yawOffset = Mth.wrapDegrees(yawOffset + (float)yawDelta * 0.15F);
      pitchOffset = Mth.clamp(pitchOffset + (float)pitchDelta * 0.15F, MIN_PITCH, MAX_PITCH);
   }

   public static float yawOffset() {
      return yawOffset;
   }

   public static float pitchOffset() {
      return pitchOffset;
   }

   public static void reset() {
      yawOffset = 0.0F;
      pitchOffset = 0.0F;
   }
}
