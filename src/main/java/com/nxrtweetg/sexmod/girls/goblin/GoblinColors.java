package com.nxrtweetg.sexmod.girls.goblin;

import java.util.Random;

public final class GoblinColors {
   private GoblinColors() {
   }

   public enum SkinColor {
      LIGHT_GREEN(213, 239, 150),
      MEDIUM_GREEN(189, 165, 91),
      DARK_GREEN(160, 183, 135),
      LIGHT_YELLOW(234, 176, 102),
      LIGHT_BLUE(187, 203, 252);

      private final int argb;

      SkinColor(int r, int g, int b) {
         this.argb = packArgb(r, g, b);
      }

      public int argb() {
         return this.argb;
      }

      public static SkinColor byId(int id) {
         SkinColor[] values = values();
         if (id < 0 || id >= values.length) {
            return LIGHT_GREEN;
         }
         return values[id];
      }

      public static SkinColor random(Random random) {
         SkinColor[] values = values();
         return values[random.nextInt(values.length)];
      }
   }

   public enum HairColor {
      PURPLE(103, 39, 123),
      ORANGE(251, 153, 56),
      BLACK(30, 33, 38),
      BLUE(88, 83, 186),
      BROWN(63, 35, 34),
      PINK(247, 102, 109),
      RED(241, 69, 49),
      GREEN(75, 143, 106);

      private final int argb;

      HairColor(int r, int g, int b) {
         this.argb = packArgb(r, g, b);
      }

      public int argb() {
         return this.argb;
      }

      public static HairColor byId(int id) {
         HairColor[] values = values();
         if (id < 0 || id >= values.length) {
            return PURPLE;
         }
         return values[id];
      }

      public static HairColor random(Random random) {
         HairColor[] values = values();
         return values[random.nextInt(values.length)];
      }
   }

   public enum EyeColor {
      RED(255, 0, 0),
      VIOLET(132, 30, 156),
      YELLOW(243, 247, 0),
      BROWN(105, 60, 9),
      TURKEY(0, 206, 217),
      BLUE(0, 0, 255);

      private final int argb;

      EyeColor(int r, int g, int b) {
         this.argb = packArgb(r, g, b);
      }

      public int argb() {
         return this.argb;
      }

      public static EyeColor byId(int id) {
         EyeColor[] values = values();
         if (id < 0 || id >= values.length) {
            return RED;
         }
         return values[id];
      }

      public static EyeColor random(Random random) {
         EyeColor[] values = values();
         return values[random.nextInt(values.length)];
      }
   }

   public static int packArgb(int r, int g, int b) {
      return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
   }
}
