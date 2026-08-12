package com.nxrtweetg.sexmod.girls.kobold;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class KoboldEntity extends SceneGirlEntity {
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "analCum", "analHard", "analSoft", "analStart", "attack", "attack0", "attack1", "attack2",
      "backwards_walk", "blink", "blowjobCum", "blowjobFast", "blowjobSlowL", "blowjobSlowLSwitch",
      "blowjobSlowR", "blowjobSlowRSwitch", "blowjobStart", "bowcharge", "crouch_idle", "crouch_walk",
      "fall_tree", "fly", "fly2", "idle", "idle_armed", "mating_press_cum", "mating_press_hard",
      "mating_press_soft", "mating_press_start", "null", "paymentBackpack", "run", "run_armed", "sit",
      "sleep", "walk"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("kobold", OWN_ANIMATIONS);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("kobold", Map.of());

   public KoboldEntity(EntityType<? extends KoboldEntity> entityType, Level world) {
      super(entityType, world, "Kobold");
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
}
