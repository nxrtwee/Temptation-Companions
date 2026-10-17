package com.nxrtweetg.sexmod.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@link RenderType#create} is package-private, and it is the only way to turn a {@link RenderSetup}
 * into a usable render type - the constructor is private and there is no registry to add one to.
 */
@Environment(EnvType.CLIENT)
@Mixin(RenderType.class)
public interface RenderTypeInvoker {
   @Invoker("create")
   static RenderType sexmod$create(String name, RenderSetup setup) {
      throw new AssertionError();
   }
}
