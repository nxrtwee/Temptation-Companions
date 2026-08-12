package com.nxrtweetg.sexmod.girls.goblin;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class GoblinEntity extends SceneGirlEntity {
   // Temporary fallback until goblin.animation.json is converted to GeckoLib 5-compatible syntax.
   private static final GirlAnimationSet ANIMATIONS = GirlAnimationSet.JENNY;
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("goblin", Map.of());

   public GoblinEntity(EntityType<? extends GoblinEntity> entityType, Level world) {
      super(entityType, world, "Goblin");
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
      return EnumSet.of(GirlSceneAction.ANAL, GirlSceneAction.BOOBJOB);
   }
}
