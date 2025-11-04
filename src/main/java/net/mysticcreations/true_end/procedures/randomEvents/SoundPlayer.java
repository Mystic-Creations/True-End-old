package net.mysticcreations.true_end.procedures.randomEvents;

import net.mysticcreations.true_end.TrueEnd;
import net.mysticcreations.true_end.init.Blocks;
import net.mysticcreations.true_end.network.Variables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import static net.mysticcreations.true_end.init.Dimensions.BTD;
import static net.minecraft.world.level.block.Blocks.*;

import static net.mysticcreations.true_end.init.Dimensions.NWAD;
import static net.mysticcreations.true_end.procedures.DimSwapToBTD.BlockPosRandomX;
import static net.mysticcreations.true_end.procedures.DimSwapToBTD.BlockPosRandomZ;

@Mod.EventBusSubscriber
public class SoundPlayer {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().dimension() != NWAD) if (player.level().dimension() != Level.OVERWORLD) return;

        if (!Variables.doRandomEvents) return;
        if (!(Math.random() < Variables.randomEventChance)) return;
        if (event.phase != TickEvent.Phase.END) return;

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        Level level = player.level();

        //Sound Players
        if (groundBlock(level, x, y, z) == Blocks.GRASS_BLOCK.get() || groundBlock(level, x, y, z) == GRASS_BLOCK) {
            if (Math.random() < 0.90) {
                repeatSound(player, 8, "block.grass.step");
            } else {
                repeatSound(player, 8, "block.grass.break");
            }
        }
        if (groundBlock(level, x, y, z) == SAND) {
            if (Math.random() < 0.90) {
                repeatSound(player, 8, "block.sand.step");
            } else {
                repeatSound(player, 8, "block.sand.break");
            }
        }
        if (groundBlock(level, x, y, z) == Blocks.DIRT.get()
            || groundBlock(level, x, y, z) == Blocks.GRAVEL.get()
            || groundBlock(level, x, y, z) == DIRT
            || groundBlock(level, x, y, z) == GRAVEL) {
            if (Math.random() < 0.90) {
                repeatSound(player, 8, "block.gravel.step");
            } else {
                repeatSound(player, 12, "block.gravel.break");
            }
        }
        if (groundBlock(level, x, y, z) == Blocks.STONE.get() || groundBlock(level, x, y, z) == STONE) {
            if (Math.random() < 0.40) {
                repeatSound(player, 8, "block.stone.step");
            } else {
                repeatSound(player, 10, "block.stone.break");
            }
        }
        if (groundBlock(level, x, y, z) == DEEPSLATE) {
            if (Math.random() < 0.60) {
                repeatSound(player, 8, "block.deepslate.step");
            } else {
                repeatSound(player, 16, "block.stone.break");
            }
        }
    }

    public static Block groundBlock(Level level, double x, double y, double z) {
        return level.getBlockState(BlockPos.containing(x, y - 0.5, z)).getBlock();
    }

    public static void repeatSound(ServerPlayer player, Integer delay, String soundId) {
        int soundX = BlockPosRandomX / 4;
        int soundY = 1 + (int) (Math.random() * ((8 - 1) + 1));
        int soundZ = BlockPosRandomZ / 4;
        Level level = player.level();
        if (level.isClientSide()) return;

        TrueEnd.wait(delay, () -> {
            level.playSound(
                null,
                BlockPos.containing(soundX, soundY, soundZ),
                ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(soundId)),
                SoundSource.NEUTRAL, 1, 1);
        });
    }
}