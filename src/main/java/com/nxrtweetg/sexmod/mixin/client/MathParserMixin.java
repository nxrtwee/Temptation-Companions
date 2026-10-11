package com.nxrtweetg.sexmod.mixin.client;

import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.Operator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(MathParser.class)
abstract class MathParserMixin {
   @Shadow
   protected abstract boolean isNumeric(String string);

   @Inject(method = "isLikelyVariable", at = @At("RETURN"), cancellable = true)
   private void sexmod$lenientVariableCheck(String string, CallbackInfoReturnable<Boolean> cir) {
      if (!cir.getReturnValue()) {
         if (string != null && !string.isEmpty()
               && !this.isNumeric(string)
               && !MathParser.isFunctionRegistered(string)
               && !Operator.isOperator(string)
               && !"?".equals(string)) {
            cir.setReturnValue(true);
         }
      }
   }
}
