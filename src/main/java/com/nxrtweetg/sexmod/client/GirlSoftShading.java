package com.nxrtweetg.sexmod.client;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;

/**
 * Recreates the soft body shading of the 1.12.2 original: skin-ish bones are tinted toward a warm
 * or cool colour depending on how their surface faces the dominant light source, which is what
 * reads as smooth "spheres" instead of flat polygon facets.
 */
@Environment(EnvType.CLIENT)
public final class GirlSoftShading {
   private static final Vec3 LIT_TINT = new Vec3(0.95, 0.65, 0.85);
   private static final Vec3 SHADE_TINT = new Vec3(0.0, 0.2, 0.3);
   private static final float STRENGTH = 0.1F;
   private static final Vec3 FALLBACK_LIGHT = new Vec3(0.2, 0.8, 0.0).normalize();

   private GirlSoftShading() {
   }

   /**
    * Picks the brightest neighbouring block as the light direction, rotated into the entity's
    * local space. Ambiguous ties fall back to a fixed direction, matching the original.
    */
   public static Vec3 resolveLightDirection(LivingEntity entity, float partialTick) {
      Level level = entity.level();
      BlockPos origin = BlockPos.containing(Math.floor(entity.getX()), Math.floor(entity.getY()), Math.floor(entity.getZ()));
      int brightest = 0;
      Vec3 best = null;
      boolean ambiguous = false;

      for (int x = -1; x < 2; x++) {
         for (int y = -1; y < 2; y++) {
            for (int z = -1; z < 2; z++) {
               int light = level.getMaxLocalRawBrightness(origin.offset(x, y, z));
               if (light > brightest) {
                  brightest = light;
                  best = new Vec3(x, y, z);
                  ambiguous = false;
               } else if (light == brightest && best != null) {
                  ambiguous = true;
               }
            }
         }
      }

      if (best == null || ambiguous) {
         return FALLBACK_LIGHT;
      }

      float bodyYaw = -Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
      return rotateY(new Vec3(best.x, best.y, -best.z), bodyYaw).normalize();
   }

   /** Blends the base colour toward the lit or shaded tint based on the surface angle. */
   public static int tint(int packedColor, float normalX, float normalY, float normalZ, Vec3 lightDirection) {
      double facing = normalX * lightDirection.x + normalY * lightDirection.y + normalZ * lightDirection.z;
      double weight = easeOutQuart(Math.abs(facing)) * STRENGTH;
      Vec3 target = facing > 0.0 ? LIT_TINT : SHADE_TINT;

      int alpha = packedColor >>> 24 & 0xFF;
      int red = packedColor >> 16 & 0xFF;
      int green = packedColor >> 8 & 0xFF;
      int blue = packedColor & 0xFF;

      return alpha << 24 | blend(red, target.x, weight) << 16 | blend(green, target.y, weight) << 8 | blend(blue, target.z, weight);
   }

   private static int blend(int channel, double target, double weight) {
      double normalized = channel / 255.0;
      return Mth.clamp((int)Math.round((normalized + (target - normalized) * weight) * 255.0), 0, 255);
   }

   private static double easeOutQuart(double value) {
      return 1.0 - Math.pow(1.0 - value, 4.0);
   }

   private static Vec3 rotateY(Vec3 vec, float degrees) {
      double radians = Math.toRadians(degrees);
      double sin = Math.sin(radians);
      double cos = Math.cos(radians);
      return new Vec3(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
   }
}
