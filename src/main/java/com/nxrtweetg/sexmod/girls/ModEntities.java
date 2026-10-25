package com.nxrtweetg.sexmod.girls;

import com.nxrtweetg.sexmod.SexmodMod;
import com.nxrtweetg.sexmod.girls.allie.AllieEntity;
import com.nxrtweetg.sexmod.girls.bee.BeeEntity;
import com.nxrtweetg.sexmod.girls.bia.BiaEntity;
import com.nxrtweetg.sexmod.girls.ellie.EllieEntity;
import com.nxrtweetg.sexmod.girls.galath.GalathEntity;
import com.nxrtweetg.sexmod.girls.goblin.GoblinEntity;
import com.nxrtweetg.sexmod.girls.jenny.JennyEntity;
import com.nxrtweetg.sexmod.girls.kobold.KoboldEntity;
import com.nxrtweetg.sexmod.girls.luna.LunaEntity;
import com.nxrtweetg.sexmod.girls.custom.CustomGirlEntity;
import com.nxrtweetg.sexmod.girls.manglelie.ManglelieEntity;
import com.nxrtweetg.sexmod.girls.slime.SlimeGirlEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public final class ModEntities {
   private static final boolean ENABLE_KOBOLD = true;
   private static final boolean ENABLE_GOBLIN = true;
   private static final boolean ENABLE_SLIME = true;
   private static final boolean ENABLE_ALLIE = true;
   private static final boolean ENABLE_BEE = true;
   private static final boolean ENABLE_GALATH = true;
   private static final boolean ENABLE_MANGLELIE = true;
   /** What vanilla would work out from the 1.95 hit box on its own; spelled out so the slime can differ. */
   private static final float DEFAULT_EYE_HEIGHT = 1.95F * 0.85F;
   private static final float DEFAULT_WIDTH = 0.6F;
   private static final float DEFAULT_HEIGHT = 1.95F;

   public static final EntityType<JennyEntity> JENNY = register("jenny", JennyEntity::new);
   public static final EntityType<EllieEntity> ELLIE = register("ellie", EllieEntity::new);
   public static final EntityType<BiaEntity> BIA = register("bia", BiaEntity::new);
   public static final EntityType<LunaEntity> LUNA = register("luna", LunaEntity::new);
   /** {@code fn}'s builder lowers her eyes to 1.6: she is the same height but her head sits inside her. */
   public static final EntityType<SlimeGirlEntity> SLIME = registerOptional("slime", SlimeGirlEntity::new, ENABLE_SLIME, 1.6F);
   public static final EntityType<AllieEntity> ALLIE = registerOptional("allie", AllieEntity::new, ENABLE_ALLIE);
   public static final EntityType<KoboldEntity> KOBOLD = registerOptional("kobold", KoboldEntity::new, ENABLE_KOBOLD);
   /** {@code fz}'s {@code setSize(0.3f, 1.5f)}: she is a head shorter than the girls and half as wide. */
   public static final EntityType<BeeEntity> BEE = registerOptional("bee", BeeEntity::new, ENABLE_BEE, 0.3F, 1.5F, 1.5F * 0.85F);
   public static final EntityType<GoblinEntity> GOBLIN = registerOptional("goblin", GoblinEntity::new, ENABLE_GOBLIN);
   public static final EntityType<GalathEntity> GALATH = registerOptional("galath", GalathEntity::new, ENABLE_GALATH);
   public static final EntityType<ManglelieEntity> MANGLELIE = registerOptional("manglelie", ManglelieEntity::new, ENABLE_MANGLELIE);
   public static final EntityType<CustomGirlEntity> CUSTOM_GIRL = register("custom_companion", CustomGirlEntity::new);
   /** Keyed by the id the spawn command takes, in the order they are offered. */
   private static final Map<String, EntityType<? extends SceneGirlEntity>> BY_ID = byId();

   private ModEntities() {
   }

   private static <T extends SceneGirlEntity> EntityType<T> register(String name, EntityFactory<T> factory) {
      return register(name, factory, DEFAULT_EYE_HEIGHT);
   }

   private static <T extends SceneGirlEntity> EntityType<T> register(String name, EntityFactory<T> factory, float eyeHeight) {
      return register(name, factory, DEFAULT_WIDTH, DEFAULT_HEIGHT, eyeHeight);
   }

   private static <T extends SceneGirlEntity> EntityType<T> register(String name, EntityFactory<T> factory, float width, float height, float eyeHeight) {
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath("sexmod", name);
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
      return Registry.register(
         BuiltInRegistries.ENTITY_TYPE,
         id,
         EntityType.Builder.of(factory, MobCategory.CREATURE).sized(width, height).eyeHeight(eyeHeight).clientTrackingRange(50).build(id.toString())
      );
   }

   private static <T extends SceneGirlEntity> EntityType<T> registerOptional(String name, EntityFactory<T> factory, boolean enabled) {
      return registerOptional(name, factory, enabled, DEFAULT_EYE_HEIGHT);
   }

   private static <T extends SceneGirlEntity> EntityType<T> registerOptional(String name, EntityFactory<T> factory, boolean enabled, float eyeHeight) {
      return enabled ? register(name, factory, eyeHeight) : null;
   }

   private static <T extends SceneGirlEntity> EntityType<T> registerOptional(
      String name, EntityFactory<T> factory, boolean enabled, float width, float height, float eyeHeight
   ) {
      return enabled ? register(name, factory, width, height, eyeHeight) : null;
   }

   private static Map<String, EntityType<? extends SceneGirlEntity>> byId() {
      Map<String, EntityType<? extends SceneGirlEntity>> map = new LinkedHashMap<>();
      map.put("jenny", JENNY);
      map.put("ellie", ELLIE);
      map.put("bia", BIA);
      map.put("luna", LUNA);
      putIfEnabled(map, "slime", SLIME);
      putIfEnabled(map, "allie", ALLIE);
      putIfEnabled(map, "bee", BEE);
      putIfEnabled(map, "kobold", KOBOLD);
      putIfEnabled(map, "goblin", GOBLIN);
      putIfEnabled(map, "galath", GALATH);
      putIfEnabled(map, "manglelie", MANGLELIE);
      map.put("custom_companion", CUSTOM_GIRL);
      return map;
   }

   private static void putIfEnabled(Map<String, EntityType<? extends SceneGirlEntity>> map, String name, EntityType<? extends SceneGirlEntity> type) {
      if (type != null) {
         map.put(name, type);
      }
   }

   public static Map<String, EntityType<? extends SceneGirlEntity>> all() {
      return BY_ID;
   }

   public static void register() {
      for (Map.Entry<String, EntityType<? extends SceneGirlEntity>> entry : BY_ID.entrySet()) {
         FabricDefaultAttributeRegistry.register(entry.getValue(), attributesFor(entry.getKey()));
      }

      SexmodMod.LOGGER.info("Registered {} girl entity types", BY_ID.size());
   }

   /**
    * Everyone shares Jenny's stat line unless the original gave them their own. The bee's builder is the
    * only one so far that does.
    */
   private static AttributeSupplier.Builder attributesFor(String name) {
      return "bee".equals(name) ? GirlEntity.createBeeAttributes() : GirlEntity.createGirlAttributes();
   }
}
