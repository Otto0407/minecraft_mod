package com.otto.trialcompanions.registry;

import com.otto.trialcompanions.TrialCompanions;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TrialCompanions.MOD_ID);

    // Trial key, summon ticket, etc. are registered here as each part is implemented.

    private ModItems() {}
}
