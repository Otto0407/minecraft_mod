package com.otto.trialcompanions;

import com.mojang.logging.LogUtils;
import com.otto.trialcompanions.registry.ModCreativeTabs;
import com.otto.trialcompanions.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TrialCompanions.MOD_ID)
public class TrialCompanions {
    public static final String MOD_ID = "trialcompanions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TrialCompanions(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
