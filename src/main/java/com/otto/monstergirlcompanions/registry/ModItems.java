package com.otto.monstergirlcompanions.registry;

import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import com.otto.monstergirlcompanions.item.SummonBellItem;
import com.otto.monstergirlcompanions.item.TrialKeyItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MonsterGirlCompanions.MOD_ID);

    public static final RegistryObject<Item> TRIAL_CORE = ITEMS.register("trial_core", () ->
            new BlockItem(ModBlocks.TRIAL_CORE.get(), new Item.Properties().rarity(Rarity.EPIC)));

    // Zombie girl
    public static final RegistryObject<Item> INFECTION_KEY = ITEMS.register("infection_key", () ->
            new TrialKeyItem(ModEntities.ZOMBIE_GIRL, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> INFECTION_BELL = ITEMS.register("infection_bell", () ->
            new SummonBellItem(ModEntities.ZOMBIE_GIRL_COMPANION, new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    // Spawn eggs are for testing; in survival monster girls only appear in trials.
    public static final RegistryObject<Item> ZOMBIE_GIRL_SPAWN_EGG = ITEMS.register("zombie_girl_spawn_egg", () ->
            new ForgeSpawnEggItem(ModEntities.ZOMBIE_GIRL, 0x5A8A3C, 0x2E6B8F, new Item.Properties()));

    private ModItems() {}
}
