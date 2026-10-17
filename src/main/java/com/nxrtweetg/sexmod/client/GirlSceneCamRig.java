package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * The original's per-state camera metadata, transcribed from {@code fp}'s state table.
 *
 * <p>Every scene state in the original carries five camera facts: which bone the view is mounted on
 * ({@code girlCam}, or {@code boyCam} when {@code useBoyCam} is set), a pitch window, and whether the
 * yaw window is flipped by 180. {@code e_.java} applies them every render tick, in this order: the
 * position is written first, then pitch is pushed under {@code maxGirlPitch} and after that over
 * {@code minGirlPitch}, then yaw is pulled inside a ±90 window. The max-then-min order matters - the
 * two states built with the nine-argument constructor have min and max swapped, and with this order
 * that pins their pitch instead of leaving it free.
 *
 * <p>The two bones are camera mounts the animator authored: {@code girlCam} is parented to her
 * {@code head} and {@code boyCam} to the partner's {@code Head2}, so the view rides whatever the take
 * does to that head, and a handful of animations nudge the bone itself for the thrust (jenny's
 * {@code blowjobthrust} and {@code paizuri_cum} push {@code girlCam} forward by 1.2, the
 * {@code deepthroat} pair by 0.7, {@code prone_doggy_*} pull {@code boyCam} back by 0.75).
 */
@Environment(EnvType.CLIENT)
public final class GirlSceneCamRig {
   /** Parented to her {@code head} in every geo model. */
   public static final String GIRL_CAM = "girlCam";
   /** Parented to the partner's {@code Head2}; only some of our models carry it. */
   public static final String BOY_CAM = "boyCam";

   /** The default {@code fp} row: {@code (int, bool, bool)} leaves 30 / -90 and no flip. */
   private static final GirlSceneCamRig.Rig DEFAULT = new GirlSceneCamRig.Rig(GIRL_CAM, 30.0F, -90.0F, false);

   private GirlSceneCamRig() {
   }

   /**
    * One state's camera row.
    *
    * @param bone     the mount, {@link #GIRL_CAM} or {@link #BOY_CAM}
    * @param maxPitch {@code fp.maxGirlPitch} - positive is downwards, as in vanilla
    * @param minPitch {@code fp.minGirlPitch}
    * @param flipYaw  {@code fp.flipGirlYaw}: the yaw window is turned around
    */
   public record Rig(String bone, float maxPitch, float minPitch, boolean flipYaw) {
   }

   public static GirlSceneCamRig.Rig of(GirlSceneAction action, GirlScenePhase phase) {
      return switch (action) {
         // DEEPTHROAT_SLOW/FAST/CUM: (0, true, false, true, 40, -40, false) - boyCam, a narrow window
         case DEEPTHROAT -> new GirlSceneCamRig.Rig(BOY_CAM, 40.0F, -40.0F, false);
         // PRONE_DOGGY_*: (0, true, false, true, true) - useBoyCam plus hideNameTag, default pitch
         case PRONE_DOGGY -> new GirlSceneCamRig.Rig(BOY_CAM, 30.0F, -90.0F, false);
         // REVERSE_COWGIRL_SLOW/CUM: useBoyCam, 30 / -90, flipped. The FAST pair uses the swapped
         // nine-argument overload, which pins their pitch at 30; we keep the window instead, since a
         // pinned pitch would fight the free-look the port gives the player.
         case REVERSE_COWGIRL -> new GirlSceneCamRig.Rig(BOY_CAM, 30.0F, -90.0F, true);
         // PAIZURI_*: max -56 / min -90 - her own eyes, forced up at his face
         case BOOBJOB -> new GirlSceneCamRig.Rig(GIRL_CAM, -56.0F, -90.0F, false);
         // MISSIONARY_*: 30 / -90 with the yaw window turned around
         case MISSIONARY -> new GirlSceneCamRig.Rig(GIRL_CAM, 30.0F, -90.0F, true);
         // COWGIRLSTART/SLOW/FAST/CUM: 60 / -60
         case COWGIRL -> new GirlSceneCamRig.Rig(GIRL_CAM, 60.0F, -60.0F, false);
         // KOBOLD_ANAL_*: 4 / -80, flipped
         case KOBOLD_ANAL -> new GirlSceneCamRig.Rig(GIRL_CAM, 4.0F, -80.0F, true);
         // MATING_PRESS_START/SOFT/HARD sit at -50 / -90 and the climax opens up to -30
         case MATING_PRESS -> new GirlSceneCamRig.Rig(GIRL_CAM, phase == GirlScenePhase.CLIMAX ? -30.0F : -50.0F, -90.0F, false);
         // HEAD_PAT, the BLOWJOB family, DOGGY, ANAL, STRIP, COWGIRL_SITTING_* and TOUCH_BOOBS_* all
         // take the plain three-argument constructor
         default -> DEFAULT;
      };
   }

   /**
    * Which scenes mount the camera on a cam bone unless the config says otherwise.
    *
    * <p>Only the takes whose animations actually move a cam bone start out in bone mode: everything
    * else keeps the measured {@code Head2} framing, so no hand-tuned entry is invalidated by this
    * landing. Any scene can be switched either way live from the scene control screen.
    */
   public static boolean boneByDefault(GirlSceneAction action) {
      return switch (action) {
         case BOOBJOB, FACEFUCK, DEEPTHROAT, PRONE_DOGGY -> true;
         default -> false;
      };
   }
}
