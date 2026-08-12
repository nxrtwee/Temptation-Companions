package com.nxrtweetg.sexmod.girls.state;

public enum GirlAnimationParameter {
   AUTO_HEAD(true),
   AUTO_EYES(true),
   MOVEMENT_ACTIVE(true),
   IS_DOING_ACTION(false),
   START_STRIP(false),
   START_BLOWJOB(false),
   SUCK_BLOWJOB(false),
   CUM_BLOWJOB(false),
   THRUST_BLOWJOB(false),
   START_BOOBJOB(false),
   SLOW_BOOBJOB(false),
   FAST_BOOBJOB(false),
   CUM_BOOBJOB(false),
   START_PAYMENT(false),
   PLAYER_SHOULD_BE_RENDERED(false),
   DRAW_ITEMS(false),
   START_DOGGY(false),
   WAIT_DOGGY(false),
   DOGGY_START(false),
   DOGGY_SLOW(false),
   DOGGY_FAST(false),
   DOGGY_CUM(false),
   PRONE_DOGGY_INTRO(false),
   PRONE_DOGGY_SOFT(false),
   PRONE_DOGGY_HARD(false),
   PRONE_DOGGY_CUM(false),
   DASH(false),
   HUG(false);

   private final boolean defaultValue;

   GirlAnimationParameter(boolean defaultValue) {
      this.defaultValue = defaultValue;
   }

   public boolean defaultValue() {
      return this.defaultValue;
   }
}
