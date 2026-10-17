package com.nxrtweetg.sexmod.effect;

import com.nxrtweetg.sexmod.SexmodMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

/**
 * The one status effect the mod adds. The original registered it as
 * {@code new Potion("horny potion", false, 0xFF7B08, 0, 0)} - a beneficial effect with no icon of its
 * own - and gave it no tick behaviour at all: everything that reads it reads it from outside.
 */
public final class ModEffects {
   /** {@code co.java}'s colour, kept exactly so a brewed bottle looks the same as the original's. */
   public static final int HORNY_COLOR = 0xFF7B08;
   public static final Holder<MobEffect> HORNY = register("horny", new HornyEffect());

   private ModEffects() {
   }

   private static Holder<MobEffect> register(String name, MobEffect effect) {
      return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(SexmodMod.MOD_ID, name), effect);
   }

   /** Touching the class is enough; the holder above is created by the static initialiser. */
   public static void register() {
      SexmodMod.LOGGER.info("Registered the horny effect");
   }
}
