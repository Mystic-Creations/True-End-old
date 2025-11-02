package net.mysticcreations.true_end.config;

import net.mysticcreations.true_end.TrueEnd;
import net.mysticcreations.true_end.config.serializer.GsonSerializer;
import net.mysticcreations.true_end.init.Packets;
import net.mysticcreations.true_end.network.Variables;
import net.mysticcreations.true_end.network.packets.UpdateClientConfigPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber
public class Config {
	public static final GsonSerializer serializer = new GsonSerializer("TrueEnd_COMMON");
	public static Map<String, Object> entries;

	public static void updateConfig(String key, Object value) {
		entries.put(key, value);
		serializer.serialize(entries);

		switch (key) {
			case "randomEventChance" -> Variables.randomEventChance = (double)  value;
			case "entitySpawnChance" -> Variables.entitySpawnChance = (double)  value;
			case "btdConversationDelay" -> Variables.btdConversationDelay = (int) value;
			case "showCredits" -> Variables.showCredits = (boolean) value;
			case "showFog" -> Variables.showFog = (boolean) value;
            case "doRandomEvents" -> Variables.doRandomEvents = (boolean) value;
			case "doDaytimeChange" -> Variables.doDaytimeChange = (boolean) value;
			case "clearDreamItems" -> Variables.clearDreamItems = (boolean) value;
			case "doFlashingLights" -> Variables.doFlashingLights = (boolean) value;
            case "doChatReplies" -> Variables.doChatReplies = (boolean) value;
			default -> TrueEnd.LOGGER.warn("updateConfig: unhandled key '{}'", key);
		}
	}

	public static int handleBoolean(CommandSourceStack src, String key, boolean value) {
		try {
			Config.updateConfig(key, value);
		} catch (Exception e) {
			src.sendFailure(Component.literal("Failed to update config '" + key + "': " + e.getMessage()));
			return 0;
		}
		src.sendSuccess(() -> Component.literal("Config '" + key + "' is now set to " + value), false);
		return 1;
	}
	public static int handleDouble(CommandSourceStack src, String key, double value) {
		try {
			Config.updateConfig(key, value);
		} catch (Exception e) {
			src.sendFailure(Component.literal("Failed to update config '" + key + "': " + e.getMessage()));
			return 0;
		}
		src.sendSuccess(() -> Component.literal("Config '" + key + "' is now set to " + value), false);
		return 1;
	}
	public static int handleInt(CommandSourceStack src, String key, int value) {
		try {
			Config.updateConfig(key, value);
		} catch (Exception e) {
			src.sendFailure(Component.literal("Failed to update config '" + key + "': " + e.getMessage()));
			return 0;
		}
		src.sendSuccess(() -> Component.literal("Config '" + key + "' is now set to " + value), false);
		return 1;
	}

	public static void updateClientConfig(ServerPlayer player, CommandSourceStack src, String key, boolean value) {
		updateConfig(key, value);
		handleBoolean(src, key, value);

		if (player != null && !player.level().isClientSide()) {
			Packets.sendToPlayer(
					new UpdateClientConfigPacket(key, value), player);
		}
	}
}