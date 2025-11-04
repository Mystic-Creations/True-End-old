package net.mysticcreations.true_end.procedures.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.mysticcreations.true_end.TrueEnd;
import net.mysticcreations.true_end.network.Variables;
import net.mysticcreations.true_end.procedures.randomEvents.SoundPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;
import java.util.Random;

import static net.mysticcreations.true_end.init.Dimensions.BTD;
import static net.mysticcreations.true_end.procedures.DimSwapToBTD.BlockPosRandomX;
import static net.mysticcreations.true_end.procedures.DimSwapToBTD.BlockPosRandomZ;
import static net.mysticcreations.true_end.procedures.randomEvents.TimeChange.*;

@Mod.EventBusSubscriber
public class ChatReplies {
    //Detection & Util
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        if (!Variables.doChatReplies) return;
        LevelAccessor world = event.getPlayer().serverLevel();
        ServerPlayer player = event.getPlayer();
        MinecraftServer server = player.getServer();

        if (server != null) server.execute(() -> {
            String msg = event.getMessage().getString().toLowerCase(Locale.ROOT).trim().replaceAll("[!?.-]+$", "");
            hardcodedReplies(world, msg, player);
        });
    }
    private static void sendChatReply(LevelAccessor world, String msg, Integer delay) {
        if (!world.isClientSide() && world.getServer() != null) {
            TrueEnd.wait(delay, () -> {
                MinecraftServer server = world.getServer();
                server.getPlayerList().broadcastSystemMessage(Component.literal(msg), false);
            });
        }
    }

    //Main components
    public static void hardcodedReplies(LevelAccessor world, String msg, ServerPlayer player) {
        int delay = (int)((Math.random()*50)+15);
        switch (msg) {
            case "sleep" -> sendChatReply(world, "<§kUnknown§r> No more.", delay);
            case "awake" -> sendChatReply(world, "<§kUnknown§r> You.", delay);
            case "where from" -> sendChatReply(world, "<§kUnknown§r> The fog.", delay);
            case "why is it night" -> nightReply(player);
            case "is anyone there", "anyone there" -> sendChatReply(world, "<§kUnknown§r> Yes.", delay);
            case "who are you", "what is your name", "what's your name", "whats your name"
                    -> sendChatReply(world, "<§kUnknown§r> Unknown. Forgotten.", delay);
            case "nightmare" -> sendChatReply(world, "<§kUnknown§r> Within.", delay);
            case "fuck you" -> punish(player);
            case "where am i" -> sendChatReply(world, "<§kUnknown§r> "+(int)player.getX()+"/"+(int)player.getY()+"/"+(int)player.getZ()+".", delay);
            case "where are you" -> sendChatReply(world,"<§kUnknown§r> U29tZXdoZXJlIGNsb3NlLg==", delay);
            case "hello", "hi" -> sendChatReply(world, "<§kUnknown§r> Hi.", delay);
            case "go away", "please go away", "leave me alone", "can you leave me alone", "can you go away", "please leave me alone"
                    -> sendChatReply(world, "<§kUnknown§r> I can't.", delay);
            case "song", "sing" -> entitySings(world, delay, player);
            case "i love you" -> sendChatReply(world, "<§kUnknown§r>      ?      ", delay);
            case "voices" -> sendChatReply(world, "<§kUnknown§r> Gods.", delay);
            
            //Easter eggs
            case "null" -> sendChatReply(world, "<§kUnknown§r> I'm not Null", delay);
            case "the broken script" -> sendChatReply(world, "<§kUnknown§r> Inspiration.", delay);
            case "zarsai", "zarsaivt", "shinhoa", "shinhoaz", "fireydude", "imfireydude", "olt", "one last time", "dario" -> sendChatReply(world, "<SillyMili> <3", 30);
            default -> randomReplies(world, player);
        }
    }
    public static void randomReplies(LevelAccessor world, ServerPlayer player) {
        if (player.level().dimension() == BTD) return;
        if (!(Math.random() < (Variables.randomEventChance)/48)) return;
        int delay = 15+(int)(Math.random()*46);
        String[] messages = {
                "<§kUnknown§r> This isn't real.",
                "<§kUnknown§r> Wake up.",
                "<§kUnknown§r> "+player.getName().getString().trim()+".",
                "<§kUnknown§r> They see you.",
                "<§kUnknown§r> The world changes, but not them."
        };
        String chat = messages[new Random().nextInt(messages.length)];
        sendChatReply(world, chat, delay);
    }

    //More behavior
    private static void nightReply(ServerPlayer player) {
        ServerLevel world = (ServerLevel) player.level();
        int delay = (int)((Math.random()*50)+15);
        long time = world.getGameTime() % 24000;
        boolean isDay = time >= DAY && time < NIGHT;
        if (!isDay) {
            sendChatReply(world, "<§kUnknown§r> Sleep.", delay);
        } else {
            sendChatReply(world, "<§kUnknown§r> It is not.", delay);
        }
    }
    private static void punish(ServerPlayer player) {
        int delay = (int) ((Math.random()*50)+20);
        MinecraftServer server = player.server;
        String playerName = player.getDisplayName().getString();
        String textA = "§7§o["+playerName+"'s game mode has been changed to Adventure Mode by §kUnknown§r§7§o]";
        String textS = "§7§o["+playerName+"'s game mode has been changed to Survival Mode by §kUnknown§r§7§o]";

        TrueEnd.wait(delay, () -> {
            player.setGameMode(GameType.ADVENTURE);
            server.getPlayerList().broadcastSystemMessage(Component.literal(textA), false);
            TrueEnd.wait(6000, () -> {
                player.setGameMode(GameType.SURVIVAL);
                server.getPlayerList().broadcastSystemMessage(Component.literal(textS), false);
            });
        });
    }
    private static void entitySings(LevelAccessor world, Integer delay, ServerPlayer player) {
        sendChatReply(world, "<§kUnknown§r> 4D79206661766F72697465", delay);
        Level level = player.level();

        TrueEnd.wait(delay*2, () -> {
            level.playSound(
                null,
                BlockPos.containing(player.getX()+8, player.getY(), player.getZ()-8),
                ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("true_end:daisy_bell")),
                SoundSource.NEUTRAL, 1, 1);
        });
    }
}