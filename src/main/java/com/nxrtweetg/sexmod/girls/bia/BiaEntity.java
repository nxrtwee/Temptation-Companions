package com.nxrtweetg.sexmod.girls.bia;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BiaEntity extends SceneGirlEntity {
   /** The animation names in bia.animation.json; everything else is borrowed from Jenny. */
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "anal_cum", "anal_fast", "anal_prepare", "anal_slow", "anal_start", "anal_wait", "attack0", "attack1",
      "attack2", "backwards_walk", "bowcharge", "downed", "fastwalk", "fhappy", "fly", "fly2", "headpat", "idle",
      "null", "ride", "run", "sit", "sitdown",
      "sitdownidle", "strip", "talk_horny", "talk_horny2", "talk_idle", "talk_idle2", "talk_response",
      "throwpearl", "walk", "wave", "wave_idle",
      // Only she ships these: Jenny has no copy to fall back to, so they must be listed here.
      "sitting_alt_intro", "sitting_alt_insert", "sitting_alt_soft", "sitting_alt_hard1",
      "sitting_alt_hard2", "sitting_alt_hard3", "sitting_alt_cum",
      "anal_alt_prepare", "anal_alt_wait", "anal_alt_start", "anal_alt_slow", "anal_alt_fast", "anal_alt_cum"
   );
   /** How many takes she ships per voice family; a scene wanting more falls back to Jenny. */
   private static final Map<String, Integer> OWN_SOUNDS = Map.of(
      "ahh", 8, "bjmoan", 5, "breath", 4, "giggle", 3, "hey", 4, "huh", 3, "mmm", 8
   );
   /** Where she lacks a family, she speaks it with one of her own. */
   private static final Map<String, String> SOUND_SUBSTITUTES = Map.of(
      "moan", "ahh",
      "lipsound", "mmm",
      "heavybreathing", "breath",
      "lightbreathing", "breath",
      "aftersessionmoan", "breath",
      "hmph", "huh",
      "sadoh", "huh",
      "sigh", "breath"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("bia", OWN_ANIMATIONS);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("bia", OWN_SOUNDS, SOUND_SUBSTITUTES);

   public BiaEntity(EntityType<? extends BiaEntity> entityType, Level world) {
      super(entityType, world, "Bia");
   }

   @Override
   public String characterId() {
      return "bia";
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
   protected boolean handleCustomSoundKeyframe(String keyframe) {
      switch (keyframe) {
         case "talk_hornyMSG1":
            this.sayDialogue("heya");
            this.playSoundVariant(this.sounds().voice("hey", 4), 1.0F);
            return true;
         case "talk_hornyMSG2":
            this.sayDialogue("horny");
            this.playSoundIndex(this.sounds().voice("giggle", 3), 2, 1.0F);
            return true;
         case "talk_hornyMSG3":
            this.sayDialogue("so");
            this.playSoundIndex(this.sounds().voice("breath", 4), 0, 1.0F);
            return true;
         case "talk_hornyMSG4":
            this.sayDialogue("fun");
            this.playSoundIndex(this.sounds().voice("huh", 3), 0, 1.0F);
            return true;
         case "talk_responseMSG1":
            this.sayDialogue("huh");
            this.playSoundIndex(this.sounds().voice("huh", 3), 2, 1.0F);
            return true;
         case "talk_responseMSG2":
            this.sayDialogue("iuhm");
            this.playSoundIndex(this.sounds().voice("breath", 4), 1, 1.0F);
            return true;
         case "talk_responseMSG3":
            this.sayDialogue("yes");
            this.playSoundIndex(this.sounds().voice("giggle", 3), 0, 1.0F);
            return true;
         case "headpatMSG1":
            this.sayDialogue("headpats");
            this.playSoundIndex(this.sounds().voice("breath", 4), 0, 1.0F);
            return true;
         case "headpatMSG2":
            this.sayDialogue("hmm");
            this.playSoundIndex(this.sounds().voice("mmm", 8), 0, 1.0F);
            return true;
         case "headpatMSG3":
            this.sayDialogue("huh2");
            this.playSoundIndex(this.sounds().voice("huh", 3), 0, 1.0F);
            return true;
         case "headpatMSG4":
            this.sayDialogue("thankyou");
            this.playSoundIndex(this.sounds().voice("giggle", 3), 1, 1.0F);
            return true;
         case "sitdownMSG1":
            this.sayClientLine("come here big boy~");
            this.playSoundVariant(this.sounds().voice("breath", 4), 1.0F);
            return true;
         default:
            return false;
      }
   }

   @Override
   protected Set<GirlSceneAction> sceneActions() {
      return EnumSet.of(
         GirlSceneAction.ANAL, GirlSceneAction.ANAL_ALT, GirlSceneAction.PRONE_DOGGY, GirlSceneAction.SITTING_ALT, GirlSceneAction.STRIP
      );
   }

   @Override
   protected List<InteractionSection> extraInteractionSections() {
      return List.of(this.section("action.section.affection", ItemStack.EMPTY, List.of(this.option("headpat", "action.names.headpat", ItemStack.EMPTY))));
   }
}
