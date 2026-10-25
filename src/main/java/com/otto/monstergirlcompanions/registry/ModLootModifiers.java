package com.otto.monstergirlcompanions.registry;

import com.mojang.serialization.MapCodec;
import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import com.otto.monstergirlcompanions.loot.AddItemModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MonsterGirlCompanions.MOD_ID);

    static {
        LOOT_MODIFIERS.register("add_item", AddItemModifier.CODEC);
    }

    private ModLootModifiers() {}
}
