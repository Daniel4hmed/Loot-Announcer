package com.D4.lootannouncer;

import net.runelite.client.config.*;

@ConfigGroup("lootannouncer")
public interface LootAnnouncerConfig extends Config
{
	@ConfigSection(
			name = "Settings",
			position = 0,
			description = "Settings for Loot Announcer"
	) String settingsTitle = "Settings";

	@ConfigItem(
			keyName = "lootValue",
			name = "Minimum loot value",
			description = "Minimum value before loot is announced",
			position = 0,
			section = settingsTitle
	) default int minimumLootValue() {
		return 1000000;		// 1 Million
	}
	@ConfigItem(
			keyName = "petDrops",
			name = "Notify on pet drop",
			description = "Notifies on pet drop",
			position = 1,
			section = settingsTitle
	) default boolean notifyOnPetDrop() {
		return true;
	}
	@ConfigItem(
			keyName = "itemSpawn",
			name = "Notify when items spawn",
			description = "Any item that spawns on screen and meets your minimum value",
			position = 2,
			section = settingsTitle
	) default boolean notifyOnItemSpawn() { return false; }

	@ConfigSection(
			name = "Discord Settings",
			description = "Discord Settings",
			position = 1
	) String discordSettingsTitle = "Discord Settings";

	@ConfigItem(
			keyName = "discordWebhookURL",
			name = "Discord Webhook URL",
			description = "Sends loot info to the URL",
			section = discordSettingsTitle
	) default String getDiscordWebhookURL() {
		return "";
	}
}
