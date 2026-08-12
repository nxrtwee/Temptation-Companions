package com.nxrtweetg.sexmod.girls.scene;

public enum GirlScenePhase {
   IDLE("idle"),
   /** She walks up to the player before the offer, tracking him if he backs away (original: the ab flag). */
   APPROACH("approach"),
   PAYMENT("payment"),
   /** She walks over to the bed on her own feet; the lying-down animation comes after. */
   WALKING("walking"),
   POSITIONING("positioning"),
   /** She is in place and waits for the player to walk up to her, as the original's WAITDOGGY did. */
   WAITING("waiting"),
   INTRO("intro"),
   ACTIVE("active"),
   CLIMAX("climax");

   private final String id;

   GirlScenePhase(String id) {
      this.id = id;
   }

   public String id() {
      return this.id;
   }

   public static GirlScenePhase fromId(String value) {
      for (GirlScenePhase girlscenephase : values()) {
         if (girlscenephase.id.equals(value)) {
            return girlscenephase;
         }
      }

      return IDLE;
   }
}
