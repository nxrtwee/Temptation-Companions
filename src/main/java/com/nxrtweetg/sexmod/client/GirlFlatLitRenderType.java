package com.nxrtweetg.sexmod.client;

import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.nxrtweetg.sexmod.mixin.client.RenderTypeInvoker;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * The girls' render type, with the cardinal diffuse lighting taken back out of it.
 *
 * <p>In 1.12.2 world entities got no diffuse lighting at all: GeckoLib 3 pushed a quad normal into the
 * buffer, but the entity render pass had {@code GL_LIGHTING} off, so every face of the model was drawn at
 * exactly the texture's colour. That is the whole reason her polygons are invisible in the original.
 *
 * <p>26.2 draws entities through {@code core/entity}, whose vertex shader multiplies the colour by
 * {@code min(1, (max(0,dot(n,l0)) + max(0,dot(n,l1))) * 0.6 + 0.4)} - so every facet of a curved surface
 * lands on a different shade and the faceting reads as hard edges. Her render type, {@code entity_cutout},
 * additionally carries {@code PER_FACE_LIGHTING}, which resolves the front and back facings from
 * {@code +n} and {@code -n}; with {@code withCull(false)} and 42% of the shipped cubes being zero-thickness
 * planes, the two coincident quads of a plane get very different shades, so no choice of vertex normal can
 * flatten this from the buffer side.
 *
 * <p>The shader has the branch we want already: {@code NO_CARDINAL_LIGHTING} sets
 * {@code vertexColor = Color} and ignores normals entirely, which is the 1.12.2 result exactly. Only
 * {@code breeze_wind}, {@code energy_swirl} and {@code eyes} are built with it, none of them usable here,
 * so this clones {@link RenderPipelines#ENTITY_CUTOUT} with that flag swapped in for
 * {@code PER_FACE_LIGHTING} and leaves everything else - shaders, alpha cutout, bind groups, culling,
 * depth state, vertex format - as vanilla has it.
 *
 * <p>The clone is never registered: {@code RenderPipelines.register} only feeds {@code ShaderManager}'s
 * start-up precompile list, and {@code GlDevice.getOrCompilePipeline} is a {@code computeIfAbsent} over
 * the live shader source, so an unregistered pipeline simply compiles the first time it is drawn.
 */
@Environment(EnvType.CLIENT)
public final class GirlFlatLitRenderType {
   /** The define we are dropping: front/back shading from ±normal. */
   private static final String PER_FACE_LIGHTING = "PER_FACE_LIGHTING";
   /** The define we are adding: {@code vertexColor = Color}, normals unused. */
   private static final String NO_CARDINAL_LIGHTING = "NO_CARDINAL_LIGHTING";
   private static final RenderPipeline PIPELINE = flatLit(RenderPipelines.ENTITY_CUTOUT);
   private static final Map<Identifier, RenderType> CACHE = new ConcurrentHashMap<>();

   private GirlFlatLitRenderType() {
   }

   /** The flat-lit twin of {@code RenderTypes.entityCutout(texture)}. */
   public static RenderType get(Identifier texture) {
      return CACHE.computeIfAbsent(texture, GirlFlatLitRenderType::create);
   }

   /** {@code RenderTypes.lambda$static$6}, verbatim apart from the pipeline it is handed. */
   private static RenderType create(Identifier texture) {
      RenderSetup setup = RenderSetup.builder(PIPELINE)
         .withTexture("Sampler0", texture)
         .useLightmap()
         .useOverlay()
         .affectsCrumbling()
         .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
         .createRenderSetup();
      return RenderTypeInvoker.sexmod$create("sexmod_entity_flat_lit", setup);
   }

   /**
    * Copies a pipeline field for field through the public builder, so a version that changes the entity
    * pipeline's state carries over without touching this.
    *
    * <p>Shader defines are the one thing the builder cannot take verbatim - it only accepts numeric
    * values - which is fine because the entity pipeline's single value is {@code ALPHA_CUTOUT 0.1}. A
    * non-numeric one would be dropped, so it is called out rather than silently skipped.
    */
   private static RenderPipeline flatLit(RenderPipeline source) {
      RenderPipeline.Builder builder = RenderPipeline.builder()
         .withLocation(Identifier.fromNamespaceAndPath("sexmod", "pipeline/entity_flat_lit"))
         .withDepthStencilState(Optional.ofNullable(source.getDepthStencilState()))
         .withPolygonMode(source.getPolygonMode())
         .withCull(source.isCull())
         .withPrimitiveTopology(source.getPrimitiveTopology());

      Identifier vertexShader = source.getShaders().get(ShaderType.VERTEX);
      if (vertexShader != null) {
         builder.withVertexShader(vertexShader);
      }
      Identifier fragmentShader = source.getShaders().get(ShaderType.FRAGMENT);
      if (fragmentShader != null) {
         builder.withFragmentShader(fragmentShader);
      }

      for (BindGroupLayout layout : source.getBindGroupLayouts()) {
         builder.withBindGroupLayout(layout);
      }

      List<ColorTargetState> targets = source.getColorTargetStates();
      for (int i = 0; i < targets.size(); i++) {
         ColorTargetState target = targets.get(i);
         if (target != null) {
            builder.withColorTargetState(i, target);
         }
      }

      List<VertexFormat> bindings = source.getVertexFormatBindings();
      for (int i = 0; i < bindings.size(); i++) {
         VertexFormat binding = bindings.get(i);
         if (binding != null) {
            builder.withVertexBinding(i, binding);
         }
      }

      ShaderDefines defines = source.getShaderDefines();
      defines.values().forEach((key, value) -> copyDefine(builder, key, value));
      for (String flag : defines.flags()) {
         if (!PER_FACE_LIGHTING.equals(flag)) {
            builder.withShaderDefine(flag);
         }
      }

      builder.withShaderDefine(NO_CARDINAL_LIGHTING);
      return builder.build();
   }

   private static void copyDefine(RenderPipeline.Builder builder, String key, String value) {
      try {
         builder.withShaderDefine(key, Integer.parseInt(value));
         return;
      } catch (NumberFormatException notAnInt) {
         // falls through to the float form, which is what ALPHA_CUTOUT is
      }

      try {
         builder.withShaderDefine(key, Float.parseFloat(value));
      } catch (NumberFormatException notANumber) {
         throw new IllegalStateException(
            String.format(Locale.ROOT, "entity pipeline define %s=%s cannot be rebuilt through the public builder", key, value)
         );
      }
   }
}
