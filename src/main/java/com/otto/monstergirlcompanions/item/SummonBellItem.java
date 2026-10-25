package com.otto.monstergirlcompanions.item;

import com.otto.monstergirlcompanions.entity.companion.AbstractMonsterGirlCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/** A summon bell: ringing it is consumed and calls that bell's companion, owned by the ringer, in front of them. */
public class SummonBellItem extends Item {
    private static final double SUMMON_DISTANCE = 2.0;

    private final Supplier<? extends EntityType<? extends AbstractMonsterGirlCompanion>> companion;

    public SummonBellItem(Supplier<? extends EntityType<? extends AbstractMonsterGirlCompanion>> companion, Properties properties) {
        super(properties);
        this.companion = companion;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 ahead = Vec3.directionFromRotation(0.0F, player.getYRot()).scale(SUMMON_DISTANCE);
            BlockPos pos = BlockPos.containing(player.position().add(ahead));
            AbstractMonsterGirlCompanion girl = companion.get().spawn(serverLevel, stack, player, pos, MobSpawnType.MOB_SUMMONED, true, false);
            if (girl == null) {
                return InteractionResultHolder.fail(stack);
            }
            girl.tame(player);
            girl.setYRot(player.getYRot() + 180.0F);
            girl.setYHeadRot(girl.getYRot());
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.2F);
            stack.consume(1, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
