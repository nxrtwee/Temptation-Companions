package com.nxrtweetg.sexmod.mixin.client;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import software.bernie.geckolib.loading.math.MolangQueries;
import software.bernie.geckolib.loading.math.value.Variable;

/**
 * GeckoLib 5.2 compiles Molang expressions concurrently across worker threads during resource reload,
 * but initializes MolangQueries.VARIABLES as a non-thread-safe fastutil Object2ObjectOpenHashMap.
 * In Java 21, writing to static final fields outside <clinit> throws IllegalAccessError.
 * Overwriting accessors with synchronized blocks protects the fastutil map without mutating static final fields.
 */
@Environment(EnvType.CLIENT)
@Mixin(value = MolangQueries.class, remap = false)
public abstract class MolangQueriesMixin {
   @Shadow(remap = false)
   private static Map<String, Variable> VARIABLES;

   @Shadow(remap = false)
   private static String applyPrefixAliases(String text, String properName, String... aliases) {
      throw new AssertionError();
   }

   /**
    * @author Sexmod
    * @reason Thread-safe variable existence check
    */
   @Overwrite(remap = false)
   public static boolean isExistingVariable(String name) {
      synchronized (MolangQueries.class) {
         return VARIABLES.containsKey(name);
      }
   }

   /**
    * @author Sexmod
    * @reason Thread-safe variable registration
    */
   @Overwrite(remap = false)
   static void registerVariable(Variable variable) {
      synchronized (MolangQueries.class) {
         VARIABLES.put(variable.name(), variable);
      }
   }

   /**
    * @author Sexmod
    * @reason Thread-safe variable retrieval and computeIfAbsent
    */
   @Overwrite(remap = false)
   static Variable getVariableFor(String name) {
      synchronized (MolangQueries.class) {
         return VARIABLES.computeIfAbsent(applyPrefixAliases(name, "query.", "q."), key -> new Variable(key, 0.0));
      }
   }
}
