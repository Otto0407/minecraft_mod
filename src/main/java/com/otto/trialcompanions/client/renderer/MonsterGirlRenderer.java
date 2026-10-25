package com.otto.trialcompanions.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/** Renderer shared by hostile and friendly monster girls; only the model and texture differ. */
public class MonsterGirlRenderer<T extends Mob, M extends HumanoidModel<T>> extends HumanoidMobRenderer<T, M> {
    private final ResourceLocation texture;

    public MonsterGirlRenderer(EntityRendererProvider.Context context, M model, ResourceLocation texture) {
        super(context, model, 0.5F);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
