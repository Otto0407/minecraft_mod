package com.otto.trialcompanions.client;

import com.otto.trialcompanions.TrialCompanions;
import com.otto.trialcompanions.client.model.MonsterGirlModel;
import com.otto.trialcompanions.client.model.ZombieGirlModel;
import com.otto.trialcompanions.client.renderer.MonsterGirlRenderer;
import com.otto.trialcompanions.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TrialCompanions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.MONSTER_GIRL, MonsterGirlModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ZOMBIE_GIRL.get(), context -> new MonsterGirlRenderer<>(context,
                new ZombieGirlModel<>(context.bakeLayer(ModModelLayers.MONSTER_GIRL)),
                TrialCompanions.id("textures/entity/monster_girl/zombie_girl.png")));
    }

    private ClientSetup() {}
}
