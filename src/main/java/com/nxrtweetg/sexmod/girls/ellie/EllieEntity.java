package com.nxrtweetg.sexmod.girls.ellie;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class EllieEntity extends SceneGirlEntity {
   /** The animation names in ellie.animation.json; everything else is borrowed from Jenny. */
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack0", "attack1", "attack2", "backwards_walk", "bowcharge", "carry_cum", "carry_fast", "carry_intro",
      "carry_slow1", "carry_slow2", "carry_slow3", "carry_slow4", "cowgirlcum", "cowgirlfast", "cowgirlslow2",
      "cowgirlstart", "crouchidle", "crouchwalk", "dash", "downed", "eyes", "fastwalk", "fly", "fly2", "hug",
      "hugidle", "hugselected", "idle", "missionary_cum", "missionary_fast", "missionary_slow",
      "missionary_start", "new", "null", "ride", "run", "sit", "sitdown", "sitdownidle", "strip", "throwpearl",
      "walk"
   );
   /** How many takes she ships per voice family; a scene wanting more falls back to Jenny. */
   private static final Map<String, Integer> OWN_SOUNDS = Map.ofEntries(
      Map.entry("aftersessionmoan", 5), Map.entry("ahh", 10), Map.entry("bjmoan", 13),
      Map.entry("cometomommy", 2), Map.entry("giggle", 5), Map.entry("goodboy", 2),
      Map.entry("happyoh", 3), Map.entry("heavybreathing", 9), Map.entry("hmph", 4),
      Map.entry("huh", 2), Map.entry("lightbreathing", 8), Map.entry("lipsound", 10),
      Map.entry("mmm", 9), Map.entry("moan", 9), Map.entry("mommyhorny", 2),
      Map.entry("sadoh", 2), Map.entry("sigh", 2)
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("ellie", OWN_ANIMATIONS);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("ellie", OWN_SOUNDS);

   public EllieEntity(EntityType<? extends EllieEntity> entityType, Level world) {
      super(entityType, world, "Ellie");
   }

   @Override
   public String characterId() {
      return "ellie";
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
         case "hugMSG2":
            this.sayClientLine("Hmm...");
            this.playSoundIndex(this.sounds().voice("hmph", 4), 3, 6.0F);
            return true;
         case "hugMSG3":
            this.sayClientLine("Hey!");
            this.playSoundIndex(this.sounds().voice("huh", 2), 1, 1.0F);
            return true;
         case "hugMSG4":
            this.sayDialogue("mommyhorny");
            this.playSoundVariant(this.sounds().voice("mommyhorny", 2), 0.5F);
            return true;
         case "hugMSG5":
            this.sayDialogue("whattodo");
            this.playSoundIndex(this.sounds().voice("huh", 2), 1, 6.0F);
            return true;
         case "hugselectedMSG1":
            this.sayDialogue("iknow");
            this.playSoundIndex(this.sounds().voice("hmph", 4), 3, 6.0F);
            return true;
         case "hugselectedMSG2":
            this.sayDialogue("followmedarling");
            this.playSoundIndex(this.sounds().voice("giggle", 5), 3, 6.0F);
            return true;
         case "sitdownMSG1":
            this.sayDialogue("cometomommy");
            this.playSoundVariant(this.sounds().voice("cometomommy", 2), 0.5F);
            return true;
         default:
            return false;
      }
   }

   @Override
   protected Set<GirlSceneAction> sceneActions() {
      return EnumSet.of(GirlSceneAction.FACEFUCK, GirlSceneAction.MISSIONARY, GirlSceneAction.COWGIRL, GirlSceneAction.STRIP);
   }
}
