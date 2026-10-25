package com.nxrtweetg.sexmod.girls.goblin;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import com.nxrtweetg.sexmod.util.TagValueInput;
import com.nxrtweetg.sexmod.util.TagValueOutput;

public final class GoblinEntity extends SceneGirlEntity {
   private static final EntityDataAccessor<Integer> TRACKED_SKIN_COLOR = SynchedEntityData.defineId(
      GoblinEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> TRACKED_HAIR_COLOR = SynchedEntityData.defineId(
      GoblinEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> TRACKED_EYE_COLOR = SynchedEntityData.defineId(
      GoblinEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> TRACKED_HAIR_STYLE = SynchedEntityData.defineId(
      GoblinEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> TRACKED_CROWN = SynchedEntityData.defineId(
      GoblinEntity.class, EntityDataSerializers.INT
   );

   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack0", "attack1", "attack2", "await_pick_up", "backwards_walk", "blink", "bowcharge", "breeding_2",
      "breeding_cum_1", "breeding_cum_2", "breeding_cum_3", "breeding_fast_1c", "breeding_fast_1s", "breeding_fast_3",
      "breeding_intro_1", "breeding_intro_2", "breeding_intro_3", "breeding_slow_1l", "breeding_slow_1r", "breeding_slow_3",
      "catch_1person", "catch_1personBj", "catch_1personBj_idle", "catch_3person", "catch_3personBj", "catch_3personBj_idle",
      "fly", "fly2", "idle", "jump_1", "jump_2", "jump_3", "nelson_cum", "nelson_fastc", "nelson_fasts", "nelson_intro",
      "nelson_slow", "nelson_slow2", "null", "paizuri_cum", "paizuri_fast", "paizuri_fast_countinues", "paizuri_idle",
      "paizuri_slow", "paizuri_slow2", "paizuri_start", "pick_up_1person", "pick_up_3person", "running", "shoulder_idle",
      "sit", "stand_up", "throw_1person", "throw_3person", "thrown", "walk"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("goblin", OWN_ANIMATIONS);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("goblin", Map.of(), Map.of(), false);

   public GoblinEntity(EntityType<? extends GoblinEntity> entityType, Level world) {
      super(entityType, world, "Goblin");
      if (!world.isClientSide()) {
         Random random = new Random();
         this.setSkinColorIndex(GoblinColors.SkinColor.random(random).ordinal());
         this.setHairColorIndex(GoblinColors.HairColor.random(random).ordinal());
         this.setEyeColorIndex(GoblinColors.EyeColor.random(random).ordinal());
         this.setHairStyle(random.nextInt(6));
         this.setCrown(0);
      }
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_SKIN_COLOR, GoblinColors.SkinColor.LIGHT_GREEN.ordinal());
      builder.define(TRACKED_HAIR_COLOR, GoblinColors.HairColor.PURPLE.ordinal());
      builder.define(TRACKED_EYE_COLOR, GoblinColors.EyeColor.RED.ordinal());
      builder.define(TRACKED_HAIR_STYLE, 0);
      builder.define(TRACKED_CROWN, 0);
   }

   public int skinColorIndex() {
      return this.getEntityData().get(TRACKED_SKIN_COLOR);
   }

   public void setSkinColorIndex(int index) {
      this.getEntityData().set(TRACKED_SKIN_COLOR, index);
   }

   public int hairColorIndex() {
      return this.getEntityData().get(TRACKED_HAIR_COLOR);
   }

   public void setHairColorIndex(int index) {
      this.getEntityData().set(TRACKED_HAIR_COLOR, index);
   }

   public int eyeColorIndex() {
      return this.getEntityData().get(TRACKED_EYE_COLOR);
   }

   public void setEyeColorIndex(int index) {
      this.getEntityData().set(TRACKED_EYE_COLOR, index);
   }

   public int hairStyle() {
      return this.getEntityData().get(TRACKED_HAIR_STYLE);
   }

   public void setHairStyle(int style) {
      this.getEntityData().set(TRACKED_HAIR_STYLE, style);
   }

   public int crown() {
      return this.getEntityData().get(TRACKED_CROWN);
   }

   public void setCrown(int crown) {
      this.getEntityData().set(TRACKED_CROWN, crown);
   }

   public GoblinColors.SkinColor skinColor() {
      return GoblinColors.SkinColor.byId(this.skinColorIndex());
   }

   public GoblinColors.HairColor hairColor() {
      return GoblinColors.HairColor.byId(this.hairColorIndex());
   }

   public GoblinColors.EyeColor eyeColor() {
      return GoblinColors.EyeColor.byId(this.eyeColorIndex());
   }

   @Override
   public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      TagValueOutput output = TagValueOutput.wrap(tag, this.registryAccess());
      super.addAdditionalSaveData(tag);
      output.putInt("skinColor", this.skinColorIndex());
      output.putInt("hairColor", this.hairColorIndex());
      output.putInt("eyeColor", this.eyeColorIndex());
      output.putInt("hairStyle", this.hairStyle());
      output.putInt("crown", this.crown());
   }

   @Override
   public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      TagValueInput input = TagValueInput.wrap(tag, this.registryAccess());
      super.readAdditionalSaveData(tag);
      this.setSkinColorIndex(input.getIntOr("skinColor", this.skinColorIndex()));
      this.setHairColorIndex(input.getIntOr("hairColor", this.hairColorIndex()));
      this.setEyeColorIndex(input.getIntOr("eyeColor", this.eyeColorIndex()));
      this.setHairStyle(input.getIntOr("hairStyle", this.hairStyle()));
      this.setCrown(input.getIntOr("crown", this.crown()));
   }

   @Override
   public String characterId() {
      return "goblin";
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
      return EnumSet.of(GirlSceneAction.BOOBJOB, GirlSceneAction.NELSON, GirlSceneAction.STRIP);
   }

   @Override
   public boolean hasPaymentPhase(GirlSceneAction action) {
      return false;
   }

   @Override
   public boolean hasCompanionCommands() {
      return true;
   }


   @Override
   protected boolean handleCustomSoundKeyframe(String keyframe) {
      switch (keyframe) {
         case "smallPound" -> {
            this.playSoundVariant("misc.pounding.pounding", 36, 0.25F);
            return true;
         }
         case "pound" -> {
            this.playSoundVariant("misc.pounding.pounding", 36, 1.0F);
            return true;
         }
         case "touch" -> {
            this.playSoundVariant("misc.touch.touch", 2, 3.0F);
            return true;
         }
         case "cumSound", "cum" -> {
            this.playSoundVariant("misc.smallinserts.smallinserts", 5, 2.0F);
            return true;
         }
         case "catchEh" -> {
            this.sayDialogue("catchEh");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "catchAkward" -> {
            this.sayDialogue("catchAkward");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "catchWell" -> {
            this.sayDialogue("catchWell");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "catchRather" -> {
            this.sayDialogue("catchRather");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "catchMe" -> {
            this.sayDialogue("catchMe");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "paizuriChoice" -> {
            this.sayDialogue("paizuriChoice");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "paizuriBoth" -> {
            this.sayDialogue("paizuriBoth");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "paizruiUse" -> {
            this.sayDialogue("paizruiUse");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "breedingHmm" -> {
            this.sayDialogue("breedingHmm");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "breedingFound" -> {
            this.sayDialogue("breedingFound");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         case "breedingEnough" -> {
            this.sayDialogue("breedingEnough");
            this.playSoundIndex("misc.plob.plob", 0, 1.0F);
            return true;
         }
         default -> {
            return false;
         }
      }
   }
}
