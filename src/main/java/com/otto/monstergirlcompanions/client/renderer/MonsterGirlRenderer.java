package com.otto.monstergirlcompanions.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import com.otto.monstergirlcompanions.model.BbModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Renderer shared by hostile and friendly monster girls. The texture is the one embedded in the
 * Blockbench model, uploaded the first time the entity is drawn.
 */
public class MonsterGirlRenderer<T extends Mob, M extends HumanoidModel<T>> extends HumanoidMobRenderer<T, M> {
    /** Textures already uploaded; the hostile and friendly versions of a species share one. */
    private static final Set<ResourceLocation> UPLOADED = new HashSet<>();

    private final BbModel bbModel;
    private final ResourceLocation texture;

    public MonsterGirlRenderer(EntityRendererProvider.Context context, M model, BbModel bbModel) {
        super(context, model, 0.5F);
        this.bbModel = bbModel;
        this.texture = MonsterGirlCompanions.id("dynamic/monster_girl/" + bbModel.name());
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        if (UPLOADED.add(texture)) {
            uploadTexture();
        }
        return texture;
    }

    private void uploadTexture() {
        if (bbModel.texturePng() == null) {
            MonsterGirlCompanions.LOGGER.error("Model {} has no embedded texture", bbModel.name());
            return;
        }
        try {
            NativeImage image = NativeImage.read(bbModel.texturePng());
            Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(image));
        } catch (IOException e) {
            MonsterGirlCompanions.LOGGER.error("Could not read the texture of model {}", bbModel.name(), e);
        }
    }
}
