package com.sudden.huecraft.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber
public class HueConfig {
    public static final Pair<HueConfig, ForgeConfigSpec> PAIR;

    public static ForgeConfigSpec SPEC;
    public static ForgeConfigSpec.ConfigValue<String> bridgeAddress;
    public static ForgeConfigSpec.ConfigValue<String> group;
    public static ForgeConfigSpec.ConfigValue<String> username;
    public static ForgeConfigSpec.ConfigValue<Integer> refreshRate;
    public static ForgeConfigSpec.ConfigValue<Boolean> autoStart;

    HueConfig(ForgeConfigSpec.Builder builder) {
        builder.push("HueCraft Config");
        builder.comment("Place your bridges IP address here. (Found in the Hue app.)");
        bridgeAddress = builder.define("BridgeIP", "");
        builder.comment("The group which will be affected. (If left blank, all lights will be affected.");
        group = builder.define("Group", "All");
        builder.comment("Username to use when connecting to the bridge.");
        username = builder.define("Username", "");
        builder.comment("The refresh rate of the lights. (The refresh rate of the lights. (In milliseconds, 1000 = 1 second.))");
        refreshRate = builder.define("RefreshRate", 500);
        builder.comment("Automatically start HueCraft when Minecraft starts.");
        autoStart = builder.define("AutoStart", true);
        builder.pop();
        builder.build();
    }

    static {
        Pair<HueConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder()
                .configure(HueConfig::new);
        PAIR = pair;
        SPEC = pair.getRight();
    }

    public static void saveConfig() {
        System.out.println("Saving config...");
        SPEC.save();
    }
}

