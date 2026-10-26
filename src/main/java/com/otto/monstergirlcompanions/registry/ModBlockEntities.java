package com.otto.monstergirlcompanions.registry;

import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import com.otto.monstergirlcompanions.block.TrialCoreBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MonsterGirlCompanions.MOD_ID);

    @SuppressWarnings("DataFlowIssue") // no data fixer type for mod block entities
    public static final RegistryObject<BlockEntityType<TrialCoreBlockEntity>> TRIAL_CORE = BLOCK_ENTITIES.register("trial_core", () ->
            BlockEntityType.Builder.of(TrialCoreBlockEntity::new, ModBlocks.TRIAL_CORE.get()).build(null));

    private ModBlockEntities() {}
}
