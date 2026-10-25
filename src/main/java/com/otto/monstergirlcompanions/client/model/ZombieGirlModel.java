package com.otto.monstergirlcompanions.client.model;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;

/** Monster girl body with the vanilla zombie's outstretched arms. */
public class ZombieGirlModel<T extends Mob> extends MonsterGirlModel<T> {
    public ZombieGirlModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, entity.isAggressive(), this.attackTime, ageInTicks);
    }
}
