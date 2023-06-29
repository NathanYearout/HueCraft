package com.sudden.huecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.sudden.huecraft.APIManager;
import com.sudden.huecraft.LightManager;
import com.sudden.huecraft.config.HueConfig;
import com.sudden.huecraft.hueCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = hueCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StartCommand {
    public StartCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("start")
                .executes(context -> execute()));
    }

    public static APIManager hueAPI;

    public static void initAPI(){
        System.out.println("Initializing API...");
        // Get the configuration values from HueConfig
        String userName = HueConfig.username.get();
        String bridgeAddress = HueConfig.bridgeAddress.get();
        String group = HueConfig.group.get();

        hueAPI = new APIManager(bridgeAddress, group, userName);
    }
    public static int execute() {
        initAPI();
        Player player = Minecraft.getInstance().player;

        if (!hueAPI.toString().equalsIgnoreCase("Not Connected")) {
            if (hueAPI.toString().equalsIgnoreCase("Press Button")) {
                player.sendSystemMessage(Component.literal("Please press the link button on your hue hub.")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));

                Thread connectionThread = new Thread(() -> {
                    long startTime = System.currentTimeMillis();
                    long timeout = 60 * 1000; // 60 seconds timeout
                    long interval = 1000; // 1 second interval

                    while (System.currentTimeMillis() - startTime < timeout) {
                        if (hueAPI.registerUser()) {
                            System.out.println("Registering user");
                            player.sendSystemMessage(Component.literal("Connection success.")
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(0x00FF00))));
                            return;
                        }

                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    player.sendSystemMessage(Component.literal("Connection timed out.")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
                });

                connectionThread.start();
            } else if (hueAPI.toString().equalsIgnoreCase("Ready")) {
                player.sendSystemMessage(Component.literal("Connection success.")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x00FF00))));
            }
        } else {
            player.sendSystemMessage(Component.literal("Invalid IP or unable to connect to the bridge.")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        }
        return 1;
    }

    public static void errorMessage() {
        Player player = Minecraft.getInstance().player;
        player.sendSystemMessage(Component.literal("Invalid IP or unable to connect to the bridge.")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
    }
}