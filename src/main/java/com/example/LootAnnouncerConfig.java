package com.example;

import net.runelite.client.config.*;

@ConfigGroup("lootannouncer")
public interface LootAnnouncerConfig extends Config
{
	@ConfigItem(
			keyName = "lootValue",
			name = "Loot Value",
			description = "Minimum value for loot to be announced."
	) default int minimumLootValue() {
		return 1000000;		// 1 Million
	}
//	@ConfigItem(
//			keyName = "pets",
//			name = "Pet Drop",
//			description = "Announce when you obtain a pet"
//	) default boolean petDrop() {
//		return true;
//	}

	@ConfigSection(
			name = "Discord Settings",
			description = "Discord Settings",
			position = 1
	) String discordSettings = "Discord Settings";

	@ConfigItem(
			keyName = "discordWebhookURL",
			name = "Discord Webhook URL",
			description = "Sends loot info to the URL",
			section = discordSettings
	) default String getDiscordWebhookURL() {
		return "";
	}
}
