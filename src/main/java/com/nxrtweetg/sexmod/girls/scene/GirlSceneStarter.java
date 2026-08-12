package com.nxrtweetg.sexmod.girls.scene;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.state.GirlAnimationParameter;
import net.minecraft.server.level.ServerPlayer;

public final class GirlSceneStarter {
   private GirlSceneStarter() {
   }

   public static void beginOffer(SceneGirlEntity girl, ServerPlayer player, GirlSceneAction action) {
      girl.coreState().setActivePlayerId(player.getUUID());
      // Use girl.paymentItemFor/paymentAmountFor so per-character overrides (e.g. Luna's fish) apply
      girl.coreState().setPaymentItem(girl.paymentItemFor(action));
      girl.coreState().setPaymentItemsAmount(girl.paymentAmountFor(action));
      girl.coreState().setFollowUpAction(action.followUpAction());
      girl.coreState().setShouldBeAtTarget(false);
      girl.coreState().setShouldBeAtTargetYaw(false);
      girl.coreState().setPlayerIsInPosition(false);
      girl.coreState().animationState().set(GirlAnimationParameter.AUTO_HEAD, false);
      girl.coreState().animationState().set(GirlAnimationParameter.AUTO_EYES, false);
      girl.coreState().animationState().set(GirlAnimationParameter.MOVEMENT_ACTIVE, false);
      girl.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, true);
      girl.coreState().animationState().set(GirlAnimationParameter.IS_DOING_ACTION, false);
   }
}
