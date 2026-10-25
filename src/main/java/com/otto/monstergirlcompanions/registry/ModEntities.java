package com.otto.monstergirlcompanions.registry;

import com.otto.monstergirlcompanions.MonsterGirlCompanions;
import com.otto.monstergirlcompanions.entity.monstergirl.ZombieGirl;
import com.otto.monstergirlcompanions.model.ModBbModels;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MonsterGirlCompanions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MonsterGirlCompanions.MOD_ID);

    public static final RegistryObject<EntityType<ZombieGirl>> ZOMBIE_GIRL = ENTITY_TYPES.register("zombie_girl", () ->
            EntityType.Builder.of(ZombieGirl::new, MobCategory.MONSTER)
                    // Hitbox follows the Blockbench model, so a taller or shorter model needs no code change.
                    .sized(0.6F, ModBbModels.ZOMBIE_GIRL.height())
                    .eyeHeight(ModBbModels.ZOMBIE_GIRL.eyeHeight())
                    .clientTrackingRange(8)
                    .build("zombie_girl"));

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ZOMBIE_GIRL.get(), ZombieGirl.createAttributes().build());
    }

    private ModEntities() {}
}
