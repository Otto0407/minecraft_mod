package com.otto.monstergirlcompanions.item;

import com.otto.monstergirlcompanions.block.TrialCoreBlockEntity;
import com.otto.monstergirlcompanions.entity.monstergirl.AbstractMonsterGirl;
import com.otto.monstergirlcompanions.trial.TrialStructure;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** A trial key: used on the core of a complete trial altar it is consumed and starts that key's trial. */
public class TrialKeyItem extends Item {
    private final Supplier<? extends EntityType<? extends AbstractMonsterGirl>> monsterGirl;

    public TrialKeyItem(Supplier<? extends EntityType<? extends AbstractMonsterGirl>> monsterGirl, Properties properties) {
        super(properties);
        this.monsterGirl = monsterGirl;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof TrialCoreBlockEntity core)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        TrialCoreBlockEntity.StartResult result = core.tryStart(monsterGirl.get());
        if (result == TrialCoreBlockEntity.StartResult.STARTED) {
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        if (player != null) {
            Component message = switch (result) {
                case BUSY -> Component.translatable("message.monstergirlcompanions.trial.busy");
                case INCOMPLETE -> Component.translatable("message.monstergirlcompanions.trial.incomplete");
                default -> Component.translatable("message.monstergirlcompanions.trial.too_high", TrialStructure.MAX_Y);
            };
            player.displayClientMessage(message, true);
        }
        return InteractionResult.FAIL;
    }
}
