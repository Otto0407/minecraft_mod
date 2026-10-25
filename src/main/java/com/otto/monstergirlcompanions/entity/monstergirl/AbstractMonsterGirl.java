package com.otto.monstergirlcompanions.entity.monstergirl;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Hostile monster girl shared by every species: a mini-boss that appears during a trial.
 * The friendly (companion) version of each species is a separate entity.
 */
public abstract class AbstractMonsterGirl extends Monster {
    public static final double MAX_HEALTH = 60.0;

    protected AbstractMonsterGirl(EntityType<? extends AbstractMonsterGirl> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    /** Attributes every monster girl starts from; species override the combat-specific ones. */
    public static AttributeSupplier.Builder createBaseAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // Mini-bosses stay until defeated.
        return false;
    }
}
