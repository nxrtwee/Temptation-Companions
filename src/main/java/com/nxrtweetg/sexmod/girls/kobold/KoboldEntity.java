package com.nxrtweetg.sexmod.girls.kobold;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationRole;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.state.GirlPaymentItem;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class KoboldEntity extends SceneGirlEntity {
   private static final EntityDataAccessor<Integer> TRACKED_TRIBE_COLOR = SynchedEntityData.defineId(
      KoboldEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> TRACKED_EYE_COLOR = SynchedEntityData.defineId(
      KoboldEntity.class, EntityDataSerializers.INT
   );

   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "analCum", "analHard", "analSoft", "analStart", "attack", "attack0", "attack1", "attack2",
      "backwards_walk", "blink", "blowjobCum", "blowjobFast", "blowjobSlowL", "blowjobSlowLSwitch",
      "blowjobSlowR", "blowjobSlowRSwitch", "blowjobStart", "bowcharge", "crouch_idle", "crouch_walk",
      "fall_tree", "fly", "fly2", "idle", "idle_armed", "mating_press_cum", "mating_press_hard",
      "mating_press_soft", "mating_press_start", "null", "paymentBackpack", "run", "run_armed", "sit",
      "sleep", "walk"
   );
   private static final Map<GirlAnimationRole, String> ALIASES = Map.ofEntries(
      Map.entry(GirlAnimationRole.KOBOLD_BLOWJOB_INTRO, "blowjobStart"),
      Map.entry(GirlAnimationRole.KOBOLD_BLOWJOB_SLOW, "blowjobSlowL"),
      Map.entry(GirlAnimationRole.KOBOLD_BLOWJOB_SLOW_R, "blowjobSlowR"),
      Map.entry(GirlAnimationRole.KOBOLD_BLOWJOB_SWITCH_TO_L, "blowjobSlowLSwitch"),
      Map.entry(GirlAnimationRole.KOBOLD_BLOWJOB_SWITCH_TO_R, "blowjobSlowRSwitch"),
      Map.entry(GirlAnimationRole.KOBOLD_BLOWJOB_FAST, "blowjobFast"),
      Map.entry(GirlAnimationRole.KOBOLD_BLOWJOB_CUM, "blowjobCum"),
      Map.entry(GirlAnimationRole.KOBOLD_ANAL_START, "analStart"),
      Map.entry(GirlAnimationRole.KOBOLD_ANAL_SLOW, "analSoft"),
      Map.entry(GirlAnimationRole.KOBOLD_ANAL_FAST, "analHard"),
      Map.entry(GirlAnimationRole.KOBOLD_ANAL_CUM, "analCum")
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("kobold", OWN_ANIMATIONS, ALIASES);
   private static final Map<String, Integer> OWN_SOUNDS = Map.ofEntries(
      Map.entry("bjmoan", 10),
      Map.entry("giggle", 4),
      Map.entry("haa", 7),
      Map.entry("heymaster", 6),
      Map.entry("interested", 3),
      Map.entry("lightbreathing", 8),
      Map.entry("master", 6),
      Map.entry("moan", 11),
      Map.entry("orgasm", 4),
      Map.entry("sad", 3),
      Map.entry("yep", 7)
   );
   private static final Map<String, String> SOUND_SUBSTITUTES = Map.of(
      "ahh", "haa",
      "heavybreathing", "lightbreathing",
      "aftersessionmoan", "moan",
      "mmm", "moan",
      "huh", "interested",
      "sigh", "sad",
      "sadoh", "sad",
      "happyoh", "giggle"
   );
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("kobold", OWN_SOUNDS, SOUND_SUBSTITUTES);

   public KoboldEntity(EntityType<? extends KoboldEntity> entityType, Level world) {
      super(entityType, world, "Kobold");
      if (!world.isClientSide()) {
         Random random = new Random();
         KoboldColors.TribeColor tribe = KoboldColors.TribeColor.random(random);
         this.setTribeColorIndex(tribe.ordinal());
         this.setEyeColorArgb(KoboldColors.randomEyeArgb(random));
      }
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_TRIBE_COLOR, KoboldColors.TribeColor.PURPLE.ordinal());
      builder.define(TRACKED_EYE_COLOR, KoboldColors.TribeColor.PURPLE.mainArgb());
   }

   public int tribeColorIndex() {
      return this.getEntityData().get(TRACKED_TRIBE_COLOR);
   }

   public void setTribeColorIndex(int index) {
      this.getEntityData().set(TRACKED_TRIBE_COLOR, index);
   }

   public int eyeColorArgb() {
      return this.getEntityData().get(TRACKED_EYE_COLOR);
   }

   public void setEyeColorArgb(int argb) {
      this.getEntityData().set(TRACKED_EYE_COLOR, argb);
   }

   public KoboldColors.TribeColor tribeColor() {
      return KoboldColors.TribeColor.byId(this.tribeColorIndex());
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("tribeColor", this.tribeColorIndex());
      output.putInt("eyeColor", this.eyeColorArgb());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.setTribeColorIndex(input.getIntOr("tribeColor", this.tribeColorIndex()));
      this.setEyeColorArgb(input.getIntOr("eyeColor", this.eyeColorArgb()));
   }

   @Override
   public String characterId() {
      return "kobold";
   }

   @Override
   protected GirlAnimationSet animations() {
      return ANIMATIONS;
   }

   @Override
   protected GirlSoundSet sounds() {
      return SOUNDS;
   }

   @Override
   protected Set<GirlSceneAction> sceneActions() {
      return EnumSet.of(GirlSceneAction.KOBOLD_BLOWJOB, GirlSceneAction.KOBOLD_ANAL, GirlSceneAction.MATING_PRESS);
   }

   @Override
   public boolean hasPaymentPhase(GirlSceneAction action) {
      if (this.coreState().hasMaster()) {
         return false;
      }
      return action != GirlSceneAction.MATING_PRESS;
   }

   @Override
   public boolean hasCompanionCommands() {
      return true;
   }

   @Override
   public GirlPaymentItem paymentItemFor(GirlSceneAction action) {
      return action == GirlSceneAction.KOBOLD_BLOWJOB ? GirlPaymentItem.IRON_PICKAXE : GirlPaymentItem.GOLD;
   }

   @Override
   public int paymentAmountFor(GirlSceneAction action) {
      return action == GirlSceneAction.KOBOLD_BLOWJOB ? 1 : 3;
   }

   @Override
   protected boolean handleCustomSoundKeyframe(String keyframe) {
      switch (keyframe) {
         case "cum" -> {
            this.playSoundVariant("misc.smallinserts.smallinserts", 5, 2.0F);
            return true;
         }
         case "giggle" -> {
            this.playSoundVariant("girls.kobold.giggle.giggle", 4, 1.0F);
            return true;
         }
         case "moan", "moanMating", "analHardMSG1" -> {
            this.playSoundVariant("girls.kobold.moan.moan", 10, 1.0F);
            return true;
         }
         case "orgasm" -> {
            this.playSoundVariant("girls.kobold.orgasm.orgasm", 4, 1.0F);
            return true;
         }
         case "breath", "blowjobStartbreath" -> {
            this.playSoundVariant("girls.kobold.lightbreathing.lightbreathing", 8, 0.5F);
            return true;
         }
         case "haa" -> {
            this.playSoundVariant("girls.kobold.haa.haa", 7, 0.7F);
            return true;
         }
         case "interested" -> {
            this.playSoundVariant("girls.kobold.interested.interested", 3, 1.0F);
            return true;
         }
         case "yep" -> {
            this.playSoundVariant("girls.kobold.yep.yep", 7, 1.0F);
            return true;
         }
         case "bjmoan" -> {
            this.playSoundVariant("girls.kobold.bjmoan.bjmoan", 10, 1.0F);
            return true;
         }
         case "cumMsg" -> {
            this.playSoundVariant("girls.kobold.sad.sad", 3, 1.0F);
            return true;
         }
         default -> {
            return false;
         }
      }
   }
}
