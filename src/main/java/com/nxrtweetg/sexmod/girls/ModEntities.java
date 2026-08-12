package com.nxrtweetg.sexmod.girls;

import com.nxrtweetg.sexmod.SexmodMod;
import com.nxrtweetg.sexmod.girls.allie.AllieEntity;
import com.nxrtweetg.sexmod.girls.bee.BeeEntity;
import com.nxrtweetg.sexmod.girls.bia.BiaEntity;
import com.nxrtweetg.sexmod.girls.ellie.EllieEntity;
import com.nxrtweetg.sexmod.girls.goblin.GoblinEntity;
import com.nxrtweetg.sexmod.girls.jenny.JennyEntity;
import com.nxrtweetg.sexmod.girls.kobold.KoboldEntity;
import com.nxrtweetg.sexmod.girls.luna.LunaEntity;
import com.nxrtweetg.sexmod.girls.slime.SlimeGirlEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
   private static final boolean ENABLE_KOBOLD = false;
   private static final boolean ENABLE_GOBLIN = false;
   private static final boolean ENABLE_SLIME = false;
   private static final boolean ENABLE_ALLIE = false;
   private static final boolean ENABLE_BEE = false;

   public static final EntityType<JennyEntity> JENNY = register("jenny", JennyEntity::new);
   public static final EntityType<EllieEntity> ELLIE = register("ellie", EllieEntity::new);
   public static final EntityType<BiaEntity> BIA = register("bia", BiaEntity::new);
   public static final EntityType<LunaEntity> LUNA = register("luna", LunaEntity::new);
   public static final EntityType<SlimeGirlEntity> SLIME = registerOptional("slime", SlimeGirlEntity::new, ENABLE_SLIME);
   public static final EntityType<AllieEntity> ALLIE = registerOptional("allie", AllieEntity::new, ENABLE_ALLIE);
   public static final EntityType<KoboldEntity> KOBOLD = registerOptional("kobold", KoboldEntity::new, ENABLE_KOBOLD);
   public static final EntityType<BeeEntity> BEE = registerOptional("bee", BeeEntity::new, ENABLE_BEE);
   public static final EntityType<GoblinEntity> GOBLIN = registerOptional("goblin", GoblinEntity::new, ENABLE_GOBLIN);
   /** Keyed by the id the spawn command takes, in the order they are offered. */
   private static final Map<String, EntityType<? extends SceneGirlEntity>> BY_ID = byId();

   private ModEntities() {
   }

   private static <T extends SceneGirlEntity> EntityType<T> register(String name, EntityFactory<T> factory) {
      Identifier id = Identifier.fromNamespaceAndPath("sexmod", name);
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
      return Registry.register(
         BuiltInRegistries.ENTITY_TYPE,
         id,
         EntityType.Builder.of(factory, MobCategory.CREATURE).sized(0.6F, 1.95F).clientTrackingRange(50).build(key)
      );
   }

   private static <T extends SceneGirlEntity> EntityType<T> registerOptional(String name, EntityFactory<T> factory, boolean enabled) {
      return enabled ? register(name, factory) : null;
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
      for (EntityType<? extends SceneGirlEntity> type : BY_ID.values()) {
         FabricDefaultAttributeRegistry.register(type, GirlEntity.createGirlAttributes());
      }

      SexmodMod.LOGGER.info("Registered {} girl entity types", BY_ID.size());
   }
}
