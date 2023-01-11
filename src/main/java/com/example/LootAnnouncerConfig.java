package com.example;

import net.runelite.client.config.*;

@ConfigGroup("lootannouncer")
public interface LootAnnouncerConfig extends Config
{
	@ConfigItem(
			keyName = "lootValue",
			name = "Loot Value",
			description = "Minimum value for loot to be announced."
	) default int lootValue() {
		final int DEFAULT_VALUE = 1000000;
		return DEFAULT_VALUE;
	}
	@ConfigItem(
			keyName = "pets",
			name = "Pet Drop",
			description = "Announce when you obtain a pet"
	) default boolean petDrop() {
		return true;
	}

	@ConfigSection(
			name = "Discord Settings",
			description = "Discord Settings",
			position = 1
	) String discordSettings = "Discord Settings";

	@ConfigItem(
			keyName = "discordUsername",
			name = "Discord ID",
			description = "example#1234",
			section = discordSettings
	) default String getDiscordID() {
		return null;
	}
}
