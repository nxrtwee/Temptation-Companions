package com.nxrtweetg.sexmod.client;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * A detached debug camera for scenes.
 *
 * <p>The scene pipeline still produces one anchor every frame; freecam simply replaces the final
 * camera position with its own point in space and leaves the existing scene yaw/pitch handling
 * alone. Toggling it off therefore drops the view straight back onto the normal scene camera.
 */
@Environment(EnvType.CLIENT)
public final class GirlSceneFreecam {
   private static final double MAX_DISTANCE_FROM_SCENE = 30.0;
   private static final double MIN_SPEED = 0.05;
   private static final double MAX_SPEED = 4.0;
   private static final double SPEED_STEP = 0.05;
   private static final double SPRINT_MULTIPLIER = 2.0;
   private static boolean active;
   private static Vec3 position;
   private static double horizontalSpeed = 0.45;
   private static double verticalSpeed = 0.30;

   private GirlSceneFreecam() {
   }

   public static boolean isActive() {
      return active;
   }

   public static Optional<Vec3> activePosition() {
      return active && position != null ? Optional.of(position) : Optional.empty();
   }

   public static double horizontalSpeed() {
      return horizontalSpeed;
   }

   public static double verticalSpeed() {
      return verticalSpeed;
   }

   public static void toggle(Minecraft client) {
      if (active) {
         active = false;
         position = null;
      } else {
         Optional<Vec3> optional = GirlSceneCameraState.activeAnchor();
         if (!optional.isEmpty()) {
            active = true;
            position = optional.get();
         }
      }
   }

   public static void tick(Minecraft client) {
      if (!active) {
         return;
      }

      LocalPlayer player = client.player;
      if (player == null || client.level == null || !GirlSceneCameraController.isSceneCameraActive()) {
         clear();
         return;
      }

      Optional<Vec3> optional = GirlSceneCameraState.activeAnchor();
      if (optional.isEmpty()) {
         clear();
         return;
      }

      Vec3 anchor = optional.get();
      if (position == null) {
         position = anchor;
      }

      double speedScale = client.options.keySprint.isDown() ? 2.0 : 1.0;
      float yaw = player.getYRot() + GirlSceneFreeLook.yawOffset();
      Vec3 forward = Vec3.directionFromRotation(0.0F, yaw);
      Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
      double forwardInput = axis(client.options.keyUp.isDown(), client.options.keyDown.isDown());
      double strafeInput = axis(client.options.keyRight.isDown(), client.options.keyLeft.isDown());
      Vec3 horizontal = forward.scale(forwardInput).add(right.scale(strafeInput));
      if (horizontal.lengthSqr() > 1.0E-6) {
         horizontal = horizontal.normalize().scale(horizontalSpeed * speedScale);
      } else {
         horizontal = Vec3.ZERO;
      }

      double verticalInput = axis(client.options.keyJump.isDown(), client.options.keyShift.isDown());
      Vec3 movement = horizontal.add(0.0, verticalInput * verticalSpeed * speedScale, 0.0);
      if (movement.lengthSqr() <= 0.0) {
         position = clampToScene(anchor, position);
      } else {
         position = clampToScene(anchor, position.add(movement));
      }
   }

   public static void adjustHorizontalSpeed(double delta) {
      horizontalSpeed = clampSpeed(horizontalSpeed + delta);
   }

   public static void adjustVerticalSpeed(double delta) {
      verticalSpeed = clampSpeed(verticalSpeed + delta);
   }

   public static double speedStep() {
      return SPEED_STEP;
   }

   public static void clear() {
      active = false;
      position = null;
   }

   private static double axis(boolean positive, boolean negative) {
      return (positive ? 1.0 : 0.0) - (negative ? 1.0 : 0.0);
   }

   private static Vec3 clampToScene(Vec3 anchor, Vec3 candidate) {
      Vec3 offset = candidate.subtract(anchor);
      double maxDistanceSqr = MAX_DISTANCE_FROM_SCENE * MAX_DISTANCE_FROM_SCENE;
      return offset.lengthSqr() > maxDistanceSqr ? anchor.add(offset.normalize().scale(MAX_DISTANCE_FROM_SCENE)) : candidate;
   }

   private static double clampSpeed(double value) {
      return Mth.clamp(value, MIN_SPEED, MAX_SPEED);
   }
}
