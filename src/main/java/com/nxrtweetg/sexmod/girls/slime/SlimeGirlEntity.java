package com.nxrtweetg.sexmod.girls.slime;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class SlimeGirlEntity extends SceneGirlEntity {
   /** The animation names in slime.animation.json; everything else is borrowed from Jenny. */
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack0", "attack1", "attack2", "backwards_walk", "blowjobcum", "blowjobsuck", "blowjobthrust",
      "bowcharge", "doggycum", "doggyfast", "doggygoonbed", "doggyslow", "doggystart", "doggywait",
      "dress", "fhappy", "fly", "fly2", "idle", "jumpair", "jumpend", "jumpstart", "null", "run",
      "sit", "strip", "undress", "walk"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("slime", OWN_ANIMATIONS);
   /** She ships no voice of her own, so every line falls back to Jenny. */
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("slime", Map.of());

   public SlimeGirlEntity(EntityType<? extends SlimeGirlEntity> entityType, Level world) {
      super(entityType, world, "Slime");
   }

   @Override
   public String characterId() {
      return "slime";
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
      return EnumSet.of(GirlSceneAction.BLOWJOB, GirlSceneAction.DOGGY, GirlSceneAction.STRIP);
   }
}
