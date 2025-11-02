package net.mysticcreations.true_end.config;

import net.mysticcreations.true_end.network.Variables;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.Level;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mysticcreations.true_end.TrueEnd;
import java.util.Map;
import java.util.HashMap;

@Mod.EventBusSubscriber(modid = "true_end", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigInit {
	@SubscribeEvent
	public static void clientSetup(FMLCommonSetupEvent event) {
		Map<String, Object> entries = Config.serializer.deserialize();
		entries = entries == null ? new HashMap<>() : entries;

        //Remove old keys
        entries.remove("randomEventsToggle");
        entries.remove("creditsToggle");
        entries.remove("popupsToggle");
        entries.remove("daytimeChangeToggle");
        entries.remove("flashingLights");
        entries.remove("fogToggle");
        entries.remove("doWindowPopups");

        //Create Config
		entries.putIfAbsent("randomEventChance", 0.005d);
		entries.putIfAbsent("entitySpawnChance", 0.05d);
		entries.putIfAbsent("btdConversationDelay", 40d);
        entries.putIfAbsent("clearDreamItems", true);
		entries.putIfAbsent("doRandomEvents", true);
        entries.putIfAbsent("doFlashingLights", true);
        entries.putIfAbsent("doDaytimeChange", true);
        entries.putIfAbsent("doChatReplies", true);
		entries.putIfAbsent("showFog", true);
		entries.putIfAbsent("showCredits", true);

		Variables.randomEventChance = (double) entries.get("randomEventChance");
		Variables.entitySpawnChance = (double) entries.get("entitySpawnChance");
		Variables.btdConversationDelay = Math.toIntExact(Math.round((double) entries.get("btdConversationDelay")));
		Variables.doRandomEvents = (boolean) entries.get("doRandomEvents");
		Variables.showFog = (boolean) entries.get("showFog");
		Variables.showCredits = (boolean) entries.get("showCredits");
		Variables.doFlashingLights = (boolean) entries.get("doFlashingLights");
		Variables.doDaytimeChange = (boolean) entries.get("doDaytimeChange");
		Variables.clearDreamItems = (boolean) entries.get("clearDreamItems");
        Variables.doChatReplies = (boolean) entries.get("doChatReplies");

		Config.entries = entries;
		Config.serializer.serialize(Config.entries);
		TrueEnd.LOGGER.atLevel(Level.DEBUG).withMarker(MarkerManager.getMarker("CONFIG")).log(Config.serializer.getMessage());
	}
}