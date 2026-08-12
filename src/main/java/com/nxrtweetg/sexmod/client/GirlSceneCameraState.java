package com.nxrtweetg.sexmod.client;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class GirlSceneCameraState {
   private static Vec3 activeAnchor;

   private GirlSceneCameraState() {
   }

   public static void update(Vec3 anchorPosition) {
      activeAnchor = anchorPosition;
   }

   public static Optional<Vec3> activeAnchor() {
      return Optional.ofNullable(activeAnchor);
   }

   public static void clear() {
      activeAnchor = null;
   }
}
