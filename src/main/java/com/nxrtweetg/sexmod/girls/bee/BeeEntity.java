package com.nxrtweetg.sexmod.girls.bee;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BeeEntity extends SceneGirlEntity {
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack0", "attack1", "attack2", "bowcharge", "idle", "idle_has_chest", "null", "sex_cum",
      "sex_fast", "sex_slow", "sex_start", "throw_pearl"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("bee", OWN_ANIMATIONS);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("bee", Map.of());

   public BeeEntity(EntityType<? extends BeeEntity> entityType, Level world) {
      super(entityType, world, "Bee");
   }

   @Override
   public String characterId() {
      return "bee";
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
      return EnumSet.of(GirlSceneAction.COWGIRL);
   }
}
