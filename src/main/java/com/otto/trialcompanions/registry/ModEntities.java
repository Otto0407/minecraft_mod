package com.otto.trialcompanions.registry;

import com.otto.trialcompanions.TrialCompanions;
import com.otto.trialcompanions.entity.monstergirl.ZombieGirl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = TrialCompanions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TrialCompanions.MOD_ID);

    public static final RegistryObject<EntityType<ZombieGirl>> ZOMBIE_GIRL = ENTITY_TYPES.register("zombie_girl", () ->
            EntityType.Builder.of(ZombieGirl::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("zombie_girl"));

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ZOMBIE_GIRL.get(), ZombieGirl.createAttributes().build());
    }

    private ModEntities() {}
}
