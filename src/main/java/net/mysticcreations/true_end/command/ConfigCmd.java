package net.mysticcreations.true_end.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.mysticcreations.true_end.network.Variables;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.mysticcreations.true_end.config.Config.*;

@Mod.EventBusSubscriber
public class ConfigCmd {
    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("te-config")
                .then(Commands.literal("reset")
                    .requires(s -> s.hasPermission(4))
                    .executes(ConfigCmd::resetConfig)
                )
                .then(Commands.literal("modify")
                    .then(Commands.literal("randomEventChance")
                        .requires(s -> s.hasPermission(4))
                        .executes(ctx -> getConfig(ctx.getSource(), "randomEventChance", Variables.randomEventChance))
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 0.5))
                            .executes(ctx -> handleDouble(ctx.getSource(), "randomEventChance", DoubleArgumentType.getDouble(ctx, "value")))))

                    .then(Commands.literal("entitySpawnChance")
                        .requires(s -> s.hasPermission(4))
                        .executes(ctx -> getConfig(ctx.getSource(), "entitySpawnChance", Variables.entitySpawnChance))
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1))
                            .executes(ctx -> handleDouble(ctx.getSource(), "entitySpawnChance", DoubleArgumentType.getDouble(ctx, "value")))))

                    .then(Commands.literal("btdConversationDelay")
                        .requires(s -> s.hasPermission(4))
                        .executes(ctx -> getConfig(ctx.getSource(), "btdConversationDelay", Variables.btdConversationDelay))
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 60))
                            .executes(ctx -> handleInt(ctx.getSource(), "btdConversationDelay", IntegerArgumentType.getInteger(ctx, "value")))))

                    .then(Commands.literal("showCredits")
                        .executes(ctx -> getConfig(ctx.getSource(), "showCredits", Variables.showCredits))
                        .then(Commands.literal("true")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "showCredits", true)))
                        .then(Commands.literal("false")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "showCredits", false))))

                    .then(Commands.literal("showFog")
                        .executes(ctx -> getConfig(ctx.getSource(), "showFog", Variables.showFog))
                        .then(Commands.literal("true")
                            .executes(ctx -> {
                                try {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    updateClientConfig(player, ctx.getSource(), "showFog", true);
                                } catch (Exception e) {
                                    handleBoolean(ctx.getSource(), "showFog", true);
                                }
                                return 1;
                            }))
                        .then(Commands.literal("false")
                            .executes(ctx -> {
                                try {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    updateClientConfig(player, ctx.getSource(), "showFog", false);
                                } catch (Exception e) {
                                    handleBoolean(ctx.getSource(), "showFog", false);
                                }
                                return 1;
                            })))

                    .then(Commands.literal("doRandomEvents")
                        .requires(s -> s.hasPermission(4))
                        .executes(ctx -> getConfig(ctx.getSource(), "doRandomEvents", Variables.doRandomEvents))
                        .then(Commands.literal("true")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "doRandomEvents", true)))
                        .then(Commands.literal("false")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "doRandomEvents", false))))

                    .then(Commands.literal("doFlashingLights")
                        .executes(ctx -> getConfig(ctx.getSource(), "doFlashingLights", Variables.doFlashingLights))
                        .then(Commands.literal("true")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "doFlashingLights", true)))
                        .then(Commands.literal("false")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "doFlashingLights", false))))

                    .then(Commands.literal("doDaytimeChange")
                        .requires(s -> s.hasPermission(4))
                        .executes(ctx -> getConfig(ctx.getSource(), "doDaytimeChange", Variables.doDaytimeChange))
                        .then(Commands.literal("true")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "doDaytimeChange", true)))
                        .then(Commands.literal("false")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "doDaytimeChange", false))))

                    .then(Commands.literal("doChatReplies")
                        .requires(s -> s.hasPermission(4))
                        .executes(ctx -> getConfig(ctx.getSource(), "doChatReplies", Variables.doChatReplies))
                        .then(Commands.literal("true")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "doChatReplies", true)))
                        .then(Commands.literal("false")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "doChatReplies", false))))

                    .then(Commands.literal("clearDreamItems")
                        .requires(s -> s.hasPermission(4))
                        .executes(ctx -> getConfig(ctx.getSource(), "clearDreamItems", Variables.clearDreamItems))
                        .then(Commands.literal("true")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "clearDreamItems", true)))
                        .then(Commands.literal("false")
                            .executes(ctx -> handleBoolean(ctx.getSource(), "clearDreamItems", false))))

                )
        );
    }

    private static int getConfig(CommandSourceStack src, String key, Object value) {
        src.sendSuccess(() -> Component.literal("Config '" + key + "' is currently set to " + value), false);
        return 1;
    }

    private static int resetConfig(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        updateConfig("randomEventChance", 0.005);
        updateConfig("entitySpawnChance", 0.05);
        updateConfig("btdConversationDelay", 40);
        updateConfig("doRandomEvents", true);
        updateConfig("doWindowsPopups", true);
        try {
            ServerPlayer player = src.getPlayerOrException();
            updateClientConfig(player, src, "showFog", true);
        } catch (Exception e) {
            handleBoolean(src, "showFog", true);
        }
        updateConfig("showCredits", true);
        updateConfig("doFlashingLights", true);
        updateConfig("doDaytimeChange", true);
        updateConfig("clearDreamItems", true);
        updateConfig("doChatReplies", true);
        return 1;
    }
}
