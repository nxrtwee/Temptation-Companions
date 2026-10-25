package com.nxrtweetg.sexmod.item;

import com.nxrtweetg.sexmod.girls.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public final class ModItems {
   public static final List<Item> ALL_ITEMS = new ArrayList<>();

   public static final Item ALLIES_LAMP = register("allies_lamp", AllieLampItem::new);
   public static final Item GALATH_COIN = register("galath_coin", GalathCoinItem::new);
   public static final Item DRAGON_STAFF = register("dragon_staff", DragonStaffItem::new);
   public static final Item TRIBE_EGG = register("tribe_egg", TribeEggItem::new);
   public static final Item LASSO = register("lasso", LassoItem::new);

   public static final Item JENNY_SPAWN_EGG = register("jenny_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.JENNY, p));
   public static final Item ELLIE_SPAWN_EGG = register("ellie_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.ELLIE, p));
   public static final Item BIA_SPAWN_EGG = register("bia_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.BIA, p));
   public static final Item LUNA_SPAWN_EGG = register("luna_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.LUNA, p));
   public static final Item SLIME_SPAWN_EGG = register("slime_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.SLIME, p));
   public static final Item ALLIE_SPAWN_EGG = register("allie_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.ALLIE, p));
   public static final Item KOBOLD_SPAWN_EGG = register("kobold_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.KOBOLD, p));
   public static final Item BEE_SPAWN_EGG = register("bee_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.BEE, p));
   public static final Item GOBLIN_SPAWN_EGG = register("goblin_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.GOBLIN, p));
   public static final Item GALATH_SPAWN_EGG = register("galath_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.GALATH, p));
   public static final Item MANGLELIE_SPAWN_EGG = register("manglelie_spawn_egg", p -> new GirlSpawnEggItem(ModEntities.MANGLELIE, p));

   public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB,
      ResourceLocation.fromNamespaceAndPath("sexmod", "main")
   );

   public static final CreativeModeTab TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
      .icon(() -> new ItemStack(ALLIES_LAMP))
      .title(Component.translatable("itemGroup.sexmod.items"))
      .displayItems((displayContext, entries) -> {
         for (Item item : ALL_ITEMS) {
            entries.accept(item);
         }
         entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.HORNY));
         entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.HORNY));
         entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.HORNY));
         entries.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, ModPotions.HORNY));
      })
      .build();

   private ModItems() {
   }

   private static <T extends Item> T register(String id, Function<Item.Properties, T> factory) {
      ResourceLocation identifier = ResourceLocation.fromNamespaceAndPath("sexmod", id);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, identifier);
      Item.Properties properties = new Item.Properties();
      T item = factory.apply(properties);
      Registry.register(BuiltInRegistries.ITEM, identifier, item);
      ALL_ITEMS.add(item);
      return item;
   }

   public static void register() {
      Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, TAB);
   }
}
