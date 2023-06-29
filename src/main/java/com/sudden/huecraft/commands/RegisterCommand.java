package com.sudden.huecraft.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.sudden.huecraft.config.HueConfig;
import com.sudden.huecraft.hueCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = hueCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterCommand {

    public RegisterCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("register")
                .then(Commands.argument("ip", StringArgumentType.word())
                        .then(Commands.argument("group", StringArgumentType.greedyString())
                                .executes(context -> execute(
                                        StringArgumentType.getString(context, "ip"),
                                        StringArgumentType.getString(context, "group")
                                ))
                        )
                        .executes(context -> execute(
                                StringArgumentType.getString(context, "ip"),
                                null
                        ))
                )
        );
    }

    private static final String IP_ADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private static final Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);

    private static int execute(String ipAddress, String groupName) {
        Player player = Minecraft.getInstance().player;

        System.out.println("Attempting to validate ipAddress:" + "\"" + ipAddress + "\"");

        if (isValidIPAddress(ipAddress)) {
            System.out.println("Valid IP address");
            // Set the IP address in the config
            HueConfig.bridgeAddress.set(ipAddress);

            // If groupName is not null, set the group name in the config
            HueConfig.group.set(Objects.requireNonNullElse(groupName, ""));

            // Set the group name in the config (if none set, set to "default")
            player.sendSystemMessage(Component.literal("Successfully registered the hue Bridge! Run /start").withStyle(style -> style.withColor(TextColor.fromRgb(0x00FF00))));
        } else {
            System.out.println("Invalid IP address");
            player.sendSystemMessage(Component.literal("Please enter a valid IP address. (Can be found within Hue app)").withStyle(style -> style.withColor(TextColor.parseColor("#FF0000"))));
        }

        System.out.println("Group is " + "\"" + groupName + "\"");

//        if (ipAddress.length() == 0) {
//            player.sendSystemMessage(Component.literal("Please enter a valid IP address.").withStyle(style -> style.withColor(TextColor.fromRgb(255))));
//        }
        return 1;
    }


    public static boolean isValidIPAddress(String ipAddress) {
        return pattern.matcher(ipAddress).matches();
    }
}

