package com.otto.monstergirlcompanions;

import com.mojang.logging.LogUtils;
import com.otto.monstergirlcompanions.registry.ModBlockEntities;
import com.otto.monstergirlcompanions.registry.ModBlocks;
import com.otto.monstergirlcompanions.registry.ModCreativeTabs;
import com.otto.monstergirlcompanions.registry.ModEntities;
import com.otto.monstergirlcompanions.registry.ModItems;
import com.otto.monstergirlcompanions.registry.ModLootModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MonsterGirlCompanions.MOD_ID)
public class MonsterGirlCompanions {
    public static final String MOD_ID = "monstergirlcompanions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MonsterGirlCompanions(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
