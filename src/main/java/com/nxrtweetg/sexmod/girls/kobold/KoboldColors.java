package com.nxrtweetg.sexmod.girls.kobold;

import java.util.Random;

public final class KoboldColors {
   private KoboldColors() {
   }

   public enum TribeColor {
      GREEN(69, 141, 113, 91, 167, 128),
      YELLOW(241, 177, 77, 255, 226, 170),
      RED(230, 27, 57, 253, 232, 239),
      PURPLE(196, 148, 207, 246, 188, 96),
      LIGHT_GREEN(170, 208, 47, 230, 214, 104),
      OLD_BLUE(173, 138, 128, 118, 151, 180),
      DARK_GREY(92, 92, 110, 198, 193, 165),
      BROWN(200, 145, 112, 253, 228, 198),
      DARK_BLUE(65, 84, 116, 104, 137, 146),
      LIGHT_BLUE(100, 163, 206, 138, 235, 242),
      SILVER(136, 136, 134, 255, 255, 255);

      private final int mainR;
      private final int mainG;
      private final int mainB;
      private final int secondaryR;
      private final int secondaryG;
      private final int secondaryB;
      private final int mainArgb;
      private final int secondaryArgb;

      TribeColor(int mr, int mg, int mb, int sr, int sg, int sb) {
         this.mainR = mr;
         this.mainG = mg;
         this.mainB = mb;
         this.secondaryR = sr;
         this.secondaryG = sg;
         this.secondaryB = sb;
         this.mainArgb = packArgb(mr, mg, mb);
         this.secondaryArgb = packArgb(sr, sg, sb);
      }

      public int mainArgb() {
         return this.mainArgb;
      }

      public int secondaryArgb() {
         return this.secondaryArgb;
      }

      public static TribeColor byId(int id) {
         TribeColor[] values = values();
         if (id < 0 || id >= values.length) {
            return PURPLE;
         }
         return values[id];
      }

      public static TribeColor random(Random random) {
         TribeColor[] values = values();
         return values[random.nextInt(values.length)];
      }

      public static TribeColor random(net.minecraft.util.RandomSource random) {
         TribeColor[] values = values();
         return values[random.nextInt(values.length)];
      }
   }

   public static int packArgb(int r, int g, int b) {
      return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
   }

   public static int randomEyeArgb(Random random) {
      TribeColor[] values = TribeColor.values();
      return values[random.nextInt(values.length)].mainArgb();
   }

   public static int randomEyeArgb(net.minecraft.util.RandomSource random) {
      TribeColor[] values = TribeColor.values();
      return values[random.nextInt(values.length)].mainArgb();
   }
}
