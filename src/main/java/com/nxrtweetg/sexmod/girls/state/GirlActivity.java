package com.nxrtweetg.sexmod.girls.state;

/**
 * What the girl is doing outside of a scene: combat, the go-home pearl throw, or being downed.
 *
 * <p>Deliberately disjoint from the scene enums. The original folded these states into the same
 * {@code Action} enum as the scene phases, which is what made its entity class so tangled; here a
 * scene always wins and simply forces the activity back to {@link #NONE}.
 */
public enum GirlActivity {
   NONE("none"),
   COMBAT_MELEE("combat_melee"),
   COMBAT_BOW("combat_bow"),
   PEARL("pearl"),
   DOWNED("downed");

   private final String id;

   GirlActivity(String id) {
      this.id = id;
   }

   public String id() {
      return this.id;
   }

   public static GirlActivity fromId(String value) {
      for (GirlActivity activity : values()) {
         if (activity.id.equals(value)) {
            return activity;
         }
      }

      return NONE;
   }
}
