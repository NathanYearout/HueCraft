package com.sudden.huecraft;

import com.sudden.huecraft.commands.RegisterCommand;
import com.sudden.huecraft.commands.StartCommand;
import com.sudden.huecraft.config.HueConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.command.ConfigCommand;

/**
 * @author Sudden
 */
@Mod(hueCraft.MODID)
public final class hueCraft {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "huecraft";

    public hueCraft() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config
        System.out.print(MODID + ": registering mod and configs...");
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, HueConfig.SPEC);
        System.out.println(" done!");

        // Register events
        System.out.print(MODID + ": registering events...");
        modEventBus.register(this);
        System.out.println("done!");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MinecraftForge.EVENT_BUS.register(new LightManager());

            // Try to automatically start LightManager
            if (HueConfig.bridgeAddress.get() != "" && HueConfig.username.get() != "" && HueConfig.autoStart.get()) {
                StartCommand.initAPI();
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ModEventSubscriber {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            new RegisterCommand(event.getDispatcher());
            new StartCommand(event.getDispatcher());

            ConfigCommand.register(event.getDispatcher());
        }
    }
}