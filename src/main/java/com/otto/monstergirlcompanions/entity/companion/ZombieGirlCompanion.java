package com.otto.monstergirlcompanions.entity.companion;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

/** Friendly zombie girl, summoned with the infection bell. */
public class ZombieGirlCompanion extends AbstractMonsterGirlCompanion {
    public ZombieGirlCompanion(EntityType<? extends ZombieGirlCompanion> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes();
    }
}
