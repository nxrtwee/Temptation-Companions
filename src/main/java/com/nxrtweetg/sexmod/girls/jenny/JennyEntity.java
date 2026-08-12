package com.nxrtweetg.sexmod.girls.jenny;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class JennyEntity extends SceneGirlEntity {
   public JennyEntity(EntityType<? extends JennyEntity> entityType, Level world) {
      super(entityType, world, "Jenny");
   }

   @Override
   public String characterId() {
      return "jenny";
   }

   @Override
   protected GirlAnimationSet animations() {
      return GirlAnimationSet.JENNY;
   }

   @Override
   protected GirlSoundSet sounds() {
      return GirlSoundSet.JENNY;
   }

   @Override
   protected Set<GirlSceneAction> sceneActions() {
      return EnumSet.of(GirlSceneAction.BLOWJOB, GirlSceneAction.BOOBJOB, GirlSceneAction.DOGGY, GirlSceneAction.STRIP);
   }
}
