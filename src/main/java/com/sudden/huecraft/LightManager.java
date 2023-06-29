package com.sudden.huecraft;

import com.sudden.huecraft.commands.StartCommand;
import com.sudden.huecraft.config.HueConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;

public class LightManager {
    private long lastApiCallTime;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && minecraft.player != null) {
                Level world = minecraft.level;
                Player player = minecraft.player;

                if (APIManager.isConnected()) {
                    long currentTime = System.currentTimeMillis();
                    long cooldownTime = HueConfig.refreshRate.get(); // Set the cooldown time in milliseconds (e.g., 1 second)

                    if (currentTime - lastApiCallTime >= cooldownTime) {
                        int lightLevel = getLightLevel(world, player);
                        int skyBrightness = getSkyBrightness(world);
                        int emissionLevel = getBlockLight(world, player);
                        int decimalColor = getBlockColor(world, player);

                        if (!world.canSeeSky(player.blockPosition()) && lightLevel <= skyBrightness) {
                            StartCommand.hueAPI.lightSet(lightLevel * 17 + (emissionLevel * 17), decimalColor, 0, 15);
                        } else {
                            StartCommand.hueAPI.lightSet(skyBrightness * 22 + (emissionLevel * 17), decimalColor, 0, 15);
                        }
                        lastApiCallTime = currentTime;
                    }
                }
            }
        }
    }

    private int getSkyBrightness(Level world) {
        double d0 = 1.0D - (double) (world.getRainLevel(1.0F) * 5.0F) / 16.0D;
        double d1 = 1.0D - (double) (world.getThunderLevel(1.0F) * 5.0F) / 16.0D;
        double d2 = 0.5D + 2.0D * Mth.clamp((double) Mth.cos(world.getTimeOfDay(1.0F) * ((float) Math.PI * 2F)), -0.25D, 0.25D);
        return 11 - (int) ((1.0D - d2 * d0 * d1) * 11.0D);
    }

    private int getLightLevel(Level world, Player player) {
        return world.getBrightness(LightLayer.SKY, player.blockPosition());
    }

    // Get light level from emissive blocks
    private int getBlockLight(Level world, Player player) {
        return world.getBrightness(LightLayer.BLOCK, player.blockPosition());
    }

    private int getBlockColor(Level world, Player player) {
        BlockPos playerPos = player.blockPosition();
        return world.getBiome(playerPos).get().getSkyColor();
    }
}
