package com.otto.monstergirlcompanions.registry;

import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import com.otto.monstergirlcompanions.block.TrialCoreBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MonsterGirlCompanions.MOD_ID);

    /** Heart of a trial altar: using a trial key on it starts that key's trial. Crafted, mined with a pickaxe. */
    public static final RegistryObject<Block> TRIAL_CORE = BLOCKS.register("trial_core", () ->
            new TrialCoreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(5.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 7)
                    .sound(SoundType.TRIAL_SPAWNER)));

    private ModBlocks() {}
}
