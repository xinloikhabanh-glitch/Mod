package com.aibuilder;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.event.TickEvent;

@Mod(AIBuilderMod.MODID)
public class AIBuilderMod {
    public static final String MODID = "aibuilder";

    public AIBuilderMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AIConfig.SPEC, "aibuilder-common.toml");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        BuildCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BuildManager.tick();
        }
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        BuildManager.clear();
    }
}
