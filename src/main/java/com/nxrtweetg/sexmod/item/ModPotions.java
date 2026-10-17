package com.nxrtweetg.sexmod.item;

import com.nxrtweetg.sexmod.SexmodMod;
import com.nxrtweetg.sexmod.effect.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

/**
 * The Horny Potion. Brewed from an awkward potion plus a dandelion.
 */
public final class ModPotions {
   /** 3 minutes duration. */
   public static final int HORNY_DURATION = 3600;

   public static final Holder<Potion> HORNY = register(
      "horny_potion",
      new Potion(
         "horny_potion",
         new MobEffectInstance(ModEffects.HORNY, HORNY_DURATION)
      )
   );

   private ModPotions() {
   }

   private static Holder<Potion> register(String name, Potion potion) {
      return Registry.registerForHolder(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath(SexmodMod.MOD_ID, name), potion);
   }

   public static void register() {
      // In 26.3, brewing recipes are data-driven via data/sexmod/recipe/brewing/
      SexmodMod.LOGGER.info("Registered the horny potion");
   }
}
