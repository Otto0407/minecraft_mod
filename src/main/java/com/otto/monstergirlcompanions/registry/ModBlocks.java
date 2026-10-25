package com.otto.monstergirlcompanions.registry;

import com.otto.monstergirlcompanions.MonsterGirlCompanions;
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

    /** Heart of a trial structure: using a trial key on it starts that key's trial. Unbreakable, like bedrock. */
    public static final RegistryObject<Block> TRIAL_CORE = BLOCKS.register("trial_core", () ->
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(state -> 7)
                    .sound(SoundType.TRIAL_SPAWNER)));

    private ModBlocks() {}
}
