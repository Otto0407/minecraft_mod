package com.otto.monstergirlcompanions.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shared monster girl body built from a Blockbench model (see {@link BbModelLayer}).
 * Keeps the model's own joint positions while animating, and holds items at the
 * {@code right_item} / {@code left_item} mount points when the model has them.
 */
public class MonsterGirlModel<T extends Mob> extends HumanoidModel<T> {
    private final List<ModelPart> rightItemChain;
    private final List<ModelPart> leftItemChain;

    public MonsterGirlModel(ModelPart root, Map<String, List<String>> mounts) {
        super(root);
        this.rightItemChain = chain(root, mounts.get(BbModelLayer.RIGHT_ITEM));
        this.leftItemChain = chain(root, mounts.get(BbModelLayer.LEFT_ITEM));
    }

    private static List<ModelPart> chain(ModelPart root, List<String> path) {
        if (path == null) {
            return null;
        }
        List<ModelPart> parts = new ArrayList<>();
        ModelPart part = root;
        for (String name : path) {
            part = part.getChild(name);
            parts.add(part);
        }
        return parts;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // HumanoidModel moves the pivots to vanilla player positions; put them back on this body.
        for (ModelPart part : new ModelPart[]{head, hat, body, rightArm, leftArm, rightLeg, leftLeg}) {
            PartPose pose = part.getInitialPose();
            part.setPos(pose.x, pose.y, pose.z);
        }
        // Keep the shoulders following the body twist of the attack swing.
        if (attackTime > 0.0F) {
            float twist = body.yRot;
            float right = -rightArm.getInitialPose().x;
            float left = leftArm.getInitialPose().x;
            rightArm.x = -Mth.cos(twist) * right;
            rightArm.z = Mth.sin(twist) * right;
            leftArm.x = Mth.cos(twist) * left;
            leftArm.z = -Mth.sin(twist) * left;
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        List<ModelPart> chain = arm == HumanoidArm.RIGHT ? rightItemChain : leftItemChain;
        if (chain == null) {
            super.translateToHand(arm, poseStack);
            return;
        }
        for (ModelPart part : chain) {
            part.translateAndRotate(poseStack);
        }
        // ItemInHandLayer then shifts the item to where a vanilla hand would be (1 px out, 10 px down,
        // 2 px forward of the shoulder); undo that so the item sits exactly on the mount point.
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.translate(side / 16.0F, -10.0F / 16.0F, 2.0F / 16.0F);
    }
}
