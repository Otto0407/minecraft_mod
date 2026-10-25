package com.otto.monstergirlcompanions.item;

import com.otto.monstergirlcompanions.entity.monstergirl.AbstractMonsterGirl;
import com.otto.monstergirlcompanions.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** A trial key: used on a trial core it is consumed and that key's monster girl appears on top of the core. */
public class TrialKeyItem extends Item {
    private final Supplier<? extends EntityType<? extends AbstractMonsterGirl>> monsterGirl;

    public TrialKeyItem(Supplier<? extends EntityType<? extends AbstractMonsterGirl>> monsterGirl, Properties properties) {
        super(properties);
        this.monsterGirl = monsterGirl;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos core = context.getClickedPos();
        if (!level.getBlockState(core).is(ModBlocks.TRIAL_CORE.get())) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            Player player = context.getPlayer();
            AbstractMonsterGirl girl = monsterGirl.get().spawn(serverLevel, core.above(), MobSpawnType.TRIGGERED);
            if (girl == null) {
                return InteractionResult.FAIL;
            }
            if (player != null && !player.getAbilities().instabuild) {
                girl.setTarget(player);
            }
            level.playSound(null, core, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
