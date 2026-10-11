package com.nxrtweetg.sexmod.girls.state;

public enum GirlFollowUpAction {
   NONE(""),
   STRIP("strip"),
   BLOWJOB("blowjob"),
   KOBOLD_BLOWJOB("kobold_blowjob"),
   DEEPTHROAT("deepthroat"),
   FACEFUCK("facefuck"),
   BOOBJOB("boobjob"),
   TOUCH_BOOBS("touch_boobs"),
   DOGGY("doggy"),
   PRONE_DOGGY("prone_doggy"),
   ANAL("anal"),
   ANAL_ALT("anal_alt"),
   KOBOLD_ANAL("kobold_anal"),
   SITTING("sitting"),
   SITTING_ALT("sitting_alt"),
   MISSIONARY("missionary"),
   MATING_PRESS("mating_press"),
   REVERSE_COWGIRL("reverse_cowgirl"),
   COWGIRL("cowgirl"),
   BEE_SEX("bee_sex"),
   THREESOME("threesome");

   private final String legacyId;

   GirlFollowUpAction(String legacyId) {
      this.legacyId = legacyId;
   }

   public String legacyId() {
      return this.legacyId;
   }

   public static GirlFollowUpAction fromLegacyId(String value) {
      for (GirlFollowUpAction girlfollowupaction : values()) {
         if (girlfollowupaction.legacyId.equals(value)) {
            return girlfollowupaction;
         }
      }

      return NONE;
   }
}
