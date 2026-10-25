package com.otto.monstergirlcompanions.registry;

import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MonsterGirlCompanions.MOD_ID);

    // Spawn eggs are for testing; in survival monster girls only appear in trials.
    public static final RegistryObject<Item> ZOMBIE_GIRL_SPAWN_EGG = ITEMS.register("zombie_girl_spawn_egg", () ->
            new ForgeSpawnEggItem(ModEntities.ZOMBIE_GIRL, 0x5A8A3C, 0x2E6B8F, new Item.Properties()));

    private ModItems() {}
}
