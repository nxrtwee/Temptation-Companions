package com.nxrtweetg.sexmod.client;

import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

/**
 * Where the scene camera sits.
 *
 * <p>The original has two camera paths and both of them read a bone every render tick. For an ordinary
 * state {@code e_.java} takes {@code girlCam}'s offset and adds it to the player's own interpolated
 * position - the player having been teleported once, on entering the state, by the hand-tuned
 * {@code em.a(side, up, forward, yawOffset, pitch)} table. For a state flagged {@code useBoyCam},
 * {@code l.java} adds {@code boyCam}'s offset to <em>her</em> position instead, which makes that one a
 * plain world position of the bone. Both bones are camera mounts the animator parented to a head, so
 * the view rides the take: that is where the thrust in {@code paizuri_cum} and {@code blowjobthrust}
 * comes from.
 *
 * <p>So this class holds two kinds of anchor. {@link #update(Vec3)} is the live one, which is what bone
 * mode and the settling phases use. {@link #update(Object, Vec3)} is the legacy hold: our own
 * {@code Head2} framing was measured with hand-tuned numbers per scene, and following the partner's
 * head every frame put a twitch in it, so that path tracks the bone through the animation blend and
 * then freezes on the average of a window long enough to cover a thrust cycle. Scenes stay on it until
 * they are switched over, which keeps every measured entry in {@code sexmod-camera.json} worth what it
 * was measured to be.
 */
@Environment(EnvType.CLIENT)
public final class GirlSceneCameraState {
   /** How long the anchor tracks the bone live after a phase change, to ride out the animation blend. */
   private static final long BLEND_MILLIS = 250L;
   /** How long it averages for after that. A thrust cycle is well under this. */
   private static final long SETTLE_MILLIS = 600L;

   private static Vec3 activeAnchor;
   private static Object holdKey;
   private static Vec3 sampleSum;
   private static int sampleCount;
   private static long holdStart;
   private static boolean settled;
   /** Set while the camera is bone-mounted; null on the legacy path, so its clamps stay off. */
   private static GirlSceneCamRig.Rig activeRig;

   private GirlSceneCameraState() {
   }

   /**
    * Publishes the state's camera row for the frame. Only bone mode sets it: the clamps are the
    * original's own framing, and applying them to a scene still framed by our measured numbers would
    * fight the tuning instead of matching it.
    */
   public static void publishRig(GirlSceneCamRig.Rig rig) {
      activeRig = rig;
   }

   public static void clearRig() {
      activeRig = null;
   }

   public static Optional<GirlSceneCamRig.Rig> activeRig() {
      return Optional.ofNullable(activeRig);
   }

   /** A phase that has no still pose to hold: the anchor just follows the bone. */
   public static void update(Vec3 anchorPosition) {
      holdKey = null;
      settled = false;
      activeAnchor = anchorPosition;
   }

   /**
    * A phase whose pose is meant to stand still. {@code key} identifies it - a new key restarts the
    * settle, the same key holds whatever the settle arrived at.
    */
   public static void update(Object key, Vec3 anchorPosition) {
      if (anchorPosition == null) {
         return;
      }

      if (!Objects.equals(key, holdKey)) {
         holdKey = key;
         holdStart = System.currentTimeMillis();
         sampleSum = Vec3.ZERO;
         sampleCount = 0;
         settled = false;
         activeAnchor = anchorPosition;
         return;
      }

      if (settled) {
         return;
      }

      long elapsed = System.currentTimeMillis() - holdStart;
      if (elapsed < BLEND_MILLIS) {
         activeAnchor = anchorPosition;
         return;
      }

      sampleSum = sampleSum.add(anchorPosition);
      sampleCount++;
      activeAnchor = sampleSum.scale(1.0 / sampleCount);
      settled = elapsed >= BLEND_MILLIS + SETTLE_MILLIS;
   }

   public static Optional<Vec3> activeAnchor() {
      return Optional.ofNullable(activeAnchor);
   }

   public static void clear() {
      activeAnchor = null;
      holdKey = null;
      sampleSum = null;
      sampleCount = 0;
      settled = false;
      activeRig = null;
   }
}
