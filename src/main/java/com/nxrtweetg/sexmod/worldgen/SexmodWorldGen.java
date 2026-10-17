package com.nxrtweetg.sexmod.worldgen;

import com.nxrtweetg.sexmod.SexmodMod;
import com.nxrtweetg.sexmod.girls.ModEntities;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.allie.AllieEntity;
import com.nxrtweetg.sexmod.girls.bee.BeeEntity;
import com.nxrtweetg.sexmod.girls.galath.GalathEntity;
import com.nxrtweetg.sexmod.girls.goblin.GoblinEntity;
import com.nxrtweetg.sexmod.girls.kobold.KoboldColors;
import com.nxrtweetg.sexmod.girls.kobold.KoboldEntity;
import com.nxrtweetg.sexmod.girls.manglelie.ManglelieEntity;
import com.nxrtweetg.sexmod.girls.slime.SlimeGirlEntity;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import com.nxrtweetg.sexmod.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.AABB;

public final class SexmodWorldGen {
   public record StructureDef(
      String name,
      List<ResourceKey<Biome>> biomes,
      int sizeX,
      int sizeY,
      int sizeZ,
      int maxSlope,
      boolean hasFoundation,
      boolean isWater,
      EntityType<? extends SceneGirlEntity> girlType,
      Vec3i interiorOffset
   ) {
   }

   public static final StructureDef JENNY = new StructureDef(
      "jenny",
      Arrays.asList(Biomes.PLAINS, Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.MEADOW, Biomes.SUNFLOWER_PLAINS),
      9, 5, 9,
      2,
      true,
      false,
      ModEntities.JENNY,
      new Vec3i(4, 1, 4)
   );

   public static final StructureDef ELLIE = new StructureDef(
      "ellie",
      Arrays.asList(Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.SNOWY_TAIGA, Biomes.DARK_FOREST),
      26, 27, 29,
      16,
      true,
      false,
      ModEntities.ELLIE,
      new Vec3i(13, 2, 14)
   );

   public static final StructureDef BIA = new StructureDef(
      "bia",
      Arrays.asList(Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST),
      11, 9, 15,
      3,
      true,
      false,
      ModEntities.BIA,
      new Vec3i(5, 1, 7)
   );

   public static final StructureDef LUNA = new StructureDef(
      "luna",
      Arrays.asList(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN),
      3, 7, 10,
      0,
      false,
      true,
      ModEntities.LUNA,
      new Vec3i(1, 2, 5)
   );

   public static final StructureDef GOBLIN = new StructureDef(
      "goblin",
      Arrays.asList(Biomes.DRIPSTONE_CAVES, Biomes.LUSH_CAVES, Biomes.PLAINS, Biomes.FOREST, Biomes.DARK_FOREST, Biomes.TAIGA),
      11, 8, 11,
      0,
      false,
      false,
      ModEntities.GOBLIN,
      new Vec3i(5, 1, 5)
   );

   private static final List<StructureDef> SURFACE_STRUCTURES = Arrays.asList(ELLIE, JENNY, BIA, LUNA);
   private static final List<StructureDef> ALL_STRUCTURES = Arrays.asList(ELLIE, JENNY, BIA, LUNA, GOBLIN);

   private SexmodWorldGen() {
   }

   private static int tickCounter = 0;

   public static void register() {
      LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
         if (key.equals(BuiltInLootTables.NETHER_BRIDGE)
               || key.equals(BuiltInLootTables.BASTION_TREASURE)
               || key.equals(BuiltInLootTables.BASTION_OTHER)
               || key.equals(BuiltInLootTables.BASTION_BRIDGE)
               || key.equals(BuiltInLootTables.BASTION_HOGLIN_STABLE)) {
            LootPool.Builder pool = LootPool.lootPool()
               .setRolls(ConstantValue.exactly(1.0F))
               .when(LootItemRandomChanceCondition.randomChance(0.08F))
               .add(LootItem.lootTableItem(ModItems.ALLIES_LAMP));
            tableBuilder.pool(pool.build());
         }
      });

      net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
         tickCounter++;
         if (tickCounter % 20 != 0) {
            return;
         }
         for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.level();
            ResourceKey<Level> dim = level.dimension();
            if (dim != Level.OVERWORLD && dim != Level.NETHER) {
               continue;
            }
            ChunkPos playerChunk = ChunkPos.containing(player.blockPosition());
            for (int dx = -2; dx <= 2; dx++) {
               for (int dz = -2; dz <= 2; dz++) {
                  ChunkPos targetChunk = new ChunkPos(playerChunk.x() + dx, playerChunk.z() + dz);
                  if (level.getChunkSource().hasChunk(targetChunk.x(), targetChunk.z())) {
                     if (dim == Level.OVERWORLD) {
                        onOverworldChunkLoad(level, targetChunk);
                     } else if (dim == Level.NETHER) {
                        onNetherChunkLoad(level, targetChunk);
                     }
                  }
               }
            }
         }
      });
   }

   public static void onChunkLoad(ServerLevel level, ChunkPos chunkPos) {
      if (level.dimension() == Level.OVERWORLD) {
         onOverworldChunkLoad(level, chunkPos);
      } else if (level.dimension() == Level.NETHER) {
         onNetherChunkLoad(level, chunkPos);
      }
   }

   private static void onOverworldChunkLoad(ServerLevel level, ChunkPos chunkPos) {
      SexmodWorldGenData data = level.getDataStorage().computeIfAbsent(SexmodWorldGenData.TYPE);
      long chunkKey = chunkPos.pack();
      if (data.isChunkProcessed(chunkKey)) {
         return;
      }
      data.markChunkProcessed(chunkKey);

      RandomSource random = level.getRandom();

      // 1. Kobold pack spawn chance (0.004 per chunk, ~1 in 250 chunks)
      if (random.nextDouble() < 0.004) {
         trySpawnKoboldPack(level, chunkPos, random);
      }

      // 2. Goblin cave dungeon check (0.025 per chunk, ~1 in 40 chunks)
      if (random.nextDouble() < 0.025) {
         trySpawnGoblinDungeon(level, chunkPos, data, random);
      }

      // 3. Surface girl structures (Jenny, Ellie, Bia, Luna)
      trySpawnSurfaceStructures(level, chunkPos, data, random);

      // 4. Natural mob spawns for girls without structures (Bee, Slime, Allie)
      trySpawnNaturalGirlsOverworld(level, chunkPos, random);
   }

   private static void onNetherChunkLoad(ServerLevel level, ChunkPos chunkPos) {
      SexmodWorldGenData data = level.getDataStorage().computeIfAbsent(SexmodWorldGenData.TYPE);
      long chunkKey = chunkPos.pack();
      if (data.isChunkProcessed(chunkKey)) {
         return;
      }
      data.markChunkProcessed(chunkKey);

      RandomSource random = level.getRandom();

      // Rare chance in the Nether for Galath and Manglelie (0.004 per chunk, ~1 in 250 chunks)
      if (random.nextDouble() < 0.004) {
         trySpawnNetherGirls(level, chunkPos, random);
      }
   }

   private static void trySpawnKoboldPack(ServerLevel level, ChunkPos chunkPos, RandomSource random) {
      int x = chunkPos.getMinBlockX() + 8;
      int z = chunkPos.getMinBlockZ() + 8;
      int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
      BlockPos surface = new BlockPos(x, y, z);
      if (level.getBlockState(surface.below()).liquid()) {
         return;
      }

      int tribeColor = KoboldColors.TribeColor.random(random).ordinal();
      int eyeColor = KoboldColors.randomEyeArgb(random);
      double[][] offsets = {{0.0, 0.0}, {1.2, 0.8}, {-1.0, 1.1}, {0.5, -1.2}};

      for (double[] offset : offsets) {
         KoboldEntity kobold = ModEntities.KOBOLD.create(level, EntitySpawnReason.STRUCTURE);
         if (kobold != null) {
            kobold.snapTo(x + 0.5 + offset[0], y, z + 0.5 + offset[1], random.nextFloat() * 360.0F, 0.0F);
            kobold.setTribeColorIndex(tribeColor);
            kobold.setEyeColorArgb(eyeColor);
            kobold.setPersistenceRequired();
            level.addFreshEntity(kobold);
         }
      }
   }

   private static void trySpawnGoblinDungeon(ServerLevel level, ChunkPos chunkPos, SexmodWorldGenData data, RandomSource random) {
      if (!data.canPlaceStructure("goblin", chunkPos.x(), chunkPos.z())) {
         return;
      }

      int startX = chunkPos.getMinBlockX() + 2;
      int startZ = chunkPos.getMinBlockZ() + 2;

      // Scan for a cave floor between Y=50 down to Y=-40
      int foundY = Integer.MIN_VALUE;
      for (int y = 50; y >= -40; y--) {
         BlockPos probe = new BlockPos(startX + 5, y, startZ + 5);
         BlockState state = level.getBlockState(probe);
         BlockState below = level.getBlockState(probe.below());
         if (state.isAir() && (below.isSolid() && !below.liquid())) {
            boolean clear = true;
            for (int dy = 1; dy <= 4; dy++) {
               if (!level.getBlockState(probe.above(dy)).isAir()) {
                  clear = false;
                  break;
               }
            }
            if (clear) {
               foundY = y;
               break;
            }
         }
      }

      if (foundY == Integer.MIN_VALUE) {
         return;
      }

      BlockPos origin = new BlockPos(startX, foundY, startZ);
      data.addStructure("goblin", chunkPos.x(), chunkPos.z());
      placeStructureDirect(level, origin, "goblin", Rotation.NONE);

      BlockPos goblinPos = origin.offset(5, 1, 5);
      GoblinEntity goblin = ModEntities.GOBLIN.create(level, EntitySpawnReason.STRUCTURE);
      if (goblin != null) {
         goblin.snapTo(goblinPos.getX() + 0.5, goblinPos.getY(), goblinPos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
         goblin.setPersistenceRequired();
         goblin.coreState().setHomePos(goblinPos);
         level.addFreshEntity(goblin);
      }
   }

   private static void trySpawnSurfaceStructures(ServerLevel level, ChunkPos chunkPos, SexmodWorldGenData data, RandomSource random) {
      for (StructureDef def : SURFACE_STRUCTURES) {
         if (!data.canPlaceStructure(def.name(), chunkPos.x(), chunkPos.z())) {
            continue;
         }

         int startX = chunkPos.getMinBlockX();
         int startZ = chunkPos.getMinBlockZ();
         BlockPos centerPos = new BlockPos(startX + def.sizeX() / 2, 80, startZ + def.sizeZ() / 2);

         Holder<Biome> biomeHolder = level.getBiome(centerPos);
         boolean matchesBiome = false;
         for (ResourceKey<Biome> key : def.biomes()) {
            if (biomeHolder.is(key)) {
               matchesBiome = true;
               break;
            }
         }

         if (!matchesBiome) {
            continue;
         }

         int minY = Integer.MAX_VALUE;
         int maxY = Integer.MIN_VALUE;
         boolean invalidSurface = false;

         for (int x = startX; x < startX + def.sizeX(); x += Math.max(1, def.sizeX() / 3)) {
            for (int z = startZ; z < startZ + def.sizeZ(); z += Math.max(1, def.sizeZ() / 3)) {
               int h = def.isWater()
                  ? level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z)
                  : getActualGroundY(level, x, z);
               BlockState surfaceBlock = level.getBlockState(new BlockPos(x, h - 1, z));

               if (!def.isWater() && surfaceBlock.liquid()) {
                  invalidSurface = true;
                  break;
               }
               if (def.isWater() && !surfaceBlock.liquid()) {
                  invalidSurface = true;
                  break;
               }

               if (h < minY) minY = h;
               if (h > maxY) maxY = h;
            }
            if (invalidSurface) break;
         }

         if (invalidSurface) {
            continue;
         }

         if (maxY - minY > def.maxSlope()) {
            continue;
         }

         int placementY = def.isWater() ? 62 : maxY;
         BlockPos origin = new BlockPos(startX, placementY, startZ);

         data.addStructure(def.name(), chunkPos.x(), chunkPos.z());
         placeStructure(level, origin, def, Rotation.NONE);
         return; // Generate at most one structure per chunk
      }
   }

   /**
    * Natural spawns for characters who do not have their own structures:
    * - Bee: Birch forests, flower forests, sunflower plains, meadows
    * - Slime: Swamps, mangrove swamps, lush caves
    * - Allie: Deserts, badlands
    */
   private static void trySpawnNaturalGirlsOverworld(ServerLevel level, ChunkPos chunkPos, RandomSource random) {
      int checkX = chunkPos.getMinBlockX() + 8;
      int checkZ = chunkPos.getMinBlockZ() + 8;
      Holder<Biome> biome = level.getBiome(new BlockPos(checkX, 70, checkZ));

      // 1. Bee in flower & birch biomes
      if (biome.is(Biomes.BIRCH_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)
            || biome.is(Biomes.FLOWER_FOREST) || biome.is(Biomes.MEADOW) || biome.is(Biomes.SUNFLOWER_PLAINS)) {
         if (random.nextFloat() < 0.007F) {
            int x = chunkPos.getMinBlockX() + 3 + random.nextInt(10);
            int z = chunkPos.getMinBlockZ() + 3 + random.nextInt(10);
            int y = getActualGroundY(level, x, z);
            BlockPos ground = new BlockPos(x, y - 1, z);
            if (level.getBlockState(ground).isSolid() && !level.getBlockState(ground).liquid()) {
               BeeEntity bee = ModEntities.BEE.create(level, EntitySpawnReason.NATURAL);
               if (bee != null) {
                  bee.snapTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
                  bee.setPersistenceRequired();
                  level.addFreshEntity(bee);
               }
            }
         }
         return;
      }

      // 2. Slime in swamps and lush caves
      if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP) || biome.is(Biomes.LUSH_CAVES)) {
         if (random.nextFloat() < 0.008F) {
            int x = chunkPos.getMinBlockX() + 3 + random.nextInt(10);
            int z = chunkPos.getMinBlockZ() + 3 + random.nextInt(10);
            int y = getActualGroundY(level, x, z);
            BlockPos ground = new BlockPos(x, y - 1, z);
            if (level.getBlockState(ground).isSolid() || level.getBlockState(ground).is(Blocks.WATER)) {
               SlimeGirlEntity slime = ModEntities.SLIME.create(level, EntitySpawnReason.NATURAL);
               if (slime != null) {
                  slime.snapTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
                  slime.setPersistenceRequired();
                  level.addFreshEntity(slime);
               }
            }
         }
         return;
      }

   }

   /**
    * Natural spawns in the Nether for Galath and Manglelie.
    */
   private static void trySpawnNetherGirls(ServerLevel level, ChunkPos chunkPos, RandomSource random) {
      int x = chunkPos.getMinBlockX() + 3 + random.nextInt(10);
      int z = chunkPos.getMinBlockZ() + 3 + random.nextInt(10);

      // Search for solid floor in Nether (Y between 35 and 100)
      for (int y = 40; y <= 100; y++) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(pos);
         BlockState above = level.getBlockState(pos.above());
         BlockState below = level.getBlockState(pos.below());

         if (state.isAir() && above.isAir() && below.isSolid() && !below.liquid()) {
            Holder<Biome> biome = level.getBiome(pos);
            boolean spawnGalath;
            if (biome.is(Biomes.CRIMSON_FOREST) || biome.is(Biomes.SOUL_SAND_VALLEY)) {
               spawnGalath = true;
            } else if (biome.is(Biomes.WARPED_FOREST) || biome.is(Biomes.BASALT_DELTAS)) {
               spawnGalath = false;
            } else {
               spawnGalath = random.nextBoolean();
            }

            if (spawnGalath) {
               GalathEntity galath = ModEntities.GALATH.create(level, EntitySpawnReason.NATURAL);
               if (galath != null) {
                  galath.snapTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
                  galath.setPersistenceRequired();
                  level.addFreshEntity(galath);
               }
            } else {
               ManglelieEntity manglelie = ModEntities.MANGLELIE.create(level, EntitySpawnReason.NATURAL);
               if (manglelie != null) {
                  manglelie.snapTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
                  manglelie.setPersistenceRequired();
                  level.addFreshEntity(manglelie);
               }
            }
            break;
         }
      }
   }

   public static int getActualGroundY(ServerLevel level, int x, int z) {
      int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
      BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos(x, top, z);
      while (mut.getY() > level.getMinY()) {
         BlockState state = level.getBlockState(mut);
         if (state.isSolid() && !state.liquid() && !isTreeFoliageOrWood(state)) {
            return mut.getY() + 1;
         }
         mut.move(Direction.DOWN);
      }
      return top;
   }

   private static boolean isTreeFoliageOrWood(BlockState state) {
      return state.is(net.minecraft.tags.BlockTags.LEAVES)
         || state.is(net.minecraft.tags.BlockTags.LOGS)
         || state.is(Blocks.MUSHROOM_STEM)
         || state.is(Blocks.RED_MUSHROOM_BLOCK)
         || state.is(Blocks.BROWN_MUSHROOM_BLOCK);
   }

   public static boolean placeStructure(ServerLevel level, BlockPos origin, StructureDef def, Rotation rotation) {
      boolean placed = placeStructureDirect(level, origin, def.name(), rotation);
      if (!placed) {
         return false;
      }

      // Foundation
      if (def.hasFoundation()) {
         int startX = origin.getX() - 1;
         int endX = origin.getX() + def.sizeX() + 1;
         int startZ = origin.getZ() - 1;
         int endZ = origin.getZ() + def.sizeZ() + 1;
         int topY = origin.getY() - 1;
         BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

         for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
               int y = topY;
               while (y > level.getMinY()) {
                  mut.set(x, y, z);
                  BlockState state = level.getBlockState(mut);
                  if (state.isSolid() || !state.canBeReplaced()) {
                     break;
                  }
                  boolean sky = level.canSeeSky(mut.above());
                  level.setBlock(mut, sky ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.DIRT.defaultBlockState(), 2);
                  y--;
               }
            }
         }
      }

      // Ensure girl is present
      if (def.girlType() != null) {
         BlockPos girlPos = origin.offset(def.interiorOffset());
         List<SceneGirlEntity> existing = level.getEntitiesOfClass(
            SceneGirlEntity.class,
            new AABB(origin).inflate(16.0),
            girl -> girl.getType() == def.girlType()
         );

         if (existing.isEmpty()) {
            SceneGirlEntity girl = def.girlType().create(level, EntitySpawnReason.STRUCTURE);
            if (girl != null) {
               girl.snapTo(girlPos.getX() + 0.5, girlPos.getY(), girlPos.getZ() + 0.5, 0.0F, 0.0F);
               girl.setPersistenceRequired();
               girl.coreState().setHomePos(girlPos);
               level.addFreshEntity(girl);
            }
         } else {
            for (SceneGirlEntity girl : existing) {
               girl.setPersistenceRequired();
               girl.coreState().setHomePos(girlPos);
            }
         }
      }

      return true;
   }

   public static boolean placeStructureDirect(ServerLevel level, BlockPos origin, String name, Rotation rotation) {
      StructureTemplate template = level.getStructureManager()
         .get(Identifier.fromNamespaceAndPath("sexmod", name))
         .orElse(null);

      if (template == null) {
         SexmodMod.LOGGER.warn("Missing structure template: sexmod:{}", name);
         return false;
      }

      StructurePlaceSettings settings = new StructurePlaceSettings()
         .setIgnoreEntities(false)
         .setRotation(rotation)
         .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

      template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);

      // Clean up any stray structure blocks that may exist in the placed bounding box
      BoundingBox box = template.getBoundingBox(settings, origin);
      for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
         if (level.getBlockState(p).is(Blocks.STRUCTURE_BLOCK)) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
         }
      }
      return true;
   }

   public static StructureDef getDefByName(String name) {
      for (StructureDef def : ALL_STRUCTURES) {
         if (def.name().equalsIgnoreCase(name)) {
            return def;
         }
      }
      return null;
   }

   public static BlockPos findNearestStructure(ServerLevel level, BlockPos fromPos, String name) {
      SexmodWorldGenData data = level.getDataStorage().computeIfAbsent(SexmodWorldGenData.TYPE);
      SexmodWorldGenData.PlacedStructure closest = null;
      double closestDistSq = Double.MAX_VALUE;
      int chunkX = fromPos.getX() >> 4;
      int chunkZ = fromPos.getZ() >> 4;

      for (SexmodWorldGenData.PlacedStructure placed : data.getStructuresList()) {
         if (placed.type().equalsIgnoreCase(name)) {
            double dx = placed.chunkX() - chunkX;
            double dz = placed.chunkZ() - chunkZ;
            double distSq = dx * dx + dz * dz;
            if (distSq < closestDistSq) {
               closestDistSq = distSq;
               closest = placed;
            }
         }
      }

      if (closest != null) {
         int x = (closest.chunkX() << 4) + 8;
         int z = (closest.chunkZ() << 4) + 8;
         int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
         return new BlockPos(x, y, z);
      }

      // Structure has not been placed yet: find a suitable spot, place it and return its exact coords
      StructureDef def = getDefByName(name);
      if (def != null) {
         if ("goblin".equalsIgnoreCase(name)) {
            for (int r = 0; r <= 8; r++) {
               for (int cdx = -r; cdx <= r; cdx++) {
                  for (int cdz = -r; cdz <= r; cdz++) {
                     ChunkPos cp = new ChunkPos(chunkX + cdx, chunkZ + cdz);
                     int startX = cp.getMinBlockX() + 2;
                     int startZ = cp.getMinBlockZ() + 2;
                     for (int y = 45; y >= -40; y--) {
                        BlockPos probe = new BlockPos(startX + 5, y, startZ + 5);
                        BlockState state = level.getBlockState(probe);
                        BlockState below = level.getBlockState(probe.below());
                        if (state.isAir() && below.isSolid() && !below.liquid()) {
                           BlockPos origin = new BlockPos(startX, y, startZ);
                           data.addStructure("goblin", cp.x(), cp.z());
                           placeStructureDirect(level, origin, "goblin", Rotation.NONE);
                           BlockPos goblinPos = origin.offset(5, 1, 5);
                           GoblinEntity goblin = ModEntities.GOBLIN.create(level, EntitySpawnReason.STRUCTURE);
                           if (goblin != null) {
                              goblin.snapTo(goblinPos.getX() + 0.5, goblinPos.getY(), goblinPos.getZ() + 0.5, 0.0F, 0.0F);
                              goblin.setPersistenceRequired();
                              goblin.coreState().setHomePos(goblinPos);
                              level.addFreshEntity(goblin);
                           }
                           return goblinPos;
                        }
                     }
                  }
               }
            }
         } else {
            // Surface structure (Jenny, Ellie, Bia, Luna)
            com.mojang.datafixers.util.Pair<BlockPos, Holder<Biome>> pair = level.findClosestBiome3d(
               holder -> {
                  for (ResourceKey<Biome> key : def.biomes()) {
                     if (holder.is(key)) {
                        return true;
                     }
                  }
                  return false;
               },
               fromPos,
               6400,
               32,
               64
            );
            if (pair != null) {
               BlockPos bpos = pair.getFirst();
               ChunkPos targetChunk = ChunkPos.containing(bpos);
               int startX = targetChunk.getMinBlockX();
               int startZ = targetChunk.getMinBlockZ();
               int groundY = def.isWater() ? 62 : getActualGroundY(level, startX + def.sizeX() / 2, startZ + def.sizeZ() / 2);
               BlockPos origin = new BlockPos(startX, groundY, startZ);
               data.addStructure(def.name(), targetChunk.x(), targetChunk.z());
               placeStructure(level, origin, def, Rotation.NONE);
               return origin.offset(def.interiorOffset());
            }
         }
      }

      return null;
   }
}
