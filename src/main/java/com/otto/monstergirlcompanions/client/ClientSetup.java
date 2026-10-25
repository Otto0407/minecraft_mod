package com.otto.monstergirlcompanions.client;

import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import com.otto.monstergirlcompanions.client.model.MonsterGirlModel;
import com.otto.monstergirlcompanions.client.model.ZombieGirlModel;
import com.otto.monstergirlcompanions.client.renderer.MonsterGirlRenderer;
import com.otto.monstergirlcompanions.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MonsterGirlCompanions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.MONSTER_GIRL, MonsterGirlModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ZOMBIE_GIRL.get(), context -> new MonsterGirlRenderer<>(context,
                new ZombieGirlModel<>(context.bakeLayer(ModModelLayers.MONSTER_GIRL)),
                MonsterGirlCompanions.id("textures/entity/monster_girl/zombie_girl.png")));
    }

    private ClientSetup() {}
}
