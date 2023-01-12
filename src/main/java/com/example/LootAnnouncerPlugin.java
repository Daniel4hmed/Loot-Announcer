package com.example;

import com.example.discord.DiscordWebhook;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;

@Slf4j
@PluginDescriptor(
	name = "Loot Announcer"
)
public class LootAnnouncerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private LootAnnouncerConfig config;

	private HttpURLConnection connection;

	@Provides
	LootAnnouncerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(LootAnnouncerConfig.class);
	}

	@Override
	protected void startUp()
	{
		log.info("Loot Announcer started!");
	}

	@Override
	protected void shutDown()
	{
		log.info("Loot Announcer stopped!");
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned) {

		final int itemID = itemSpawned.getItem().getId();
		ItemComposition itemComposition = itemManager.getItemComposition(itemID);

		// 799 are noted items, -1 are item ID's
		final int realItemID = itemComposition.getNote() != -1 ? itemComposition.getLinkedNoteId() : itemID;

		final Item item = Item.builder()
				.ID(realItemID)
				.name(itemComposition.getMembersName())
				.grandExchangePrice(itemManager.getItemPrice(realItemID))
				.highAlchemyPrice(itemComposition.getHaPrice())
				.build();

		if (itemIsValuable(item.getGrandExchangePrice())) {
			try {
				sendAnnouncement(item);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void sendAnnouncement(Item item) throws IOException {

		// Check if Webhook URL is not empty
		if (!config.getDiscordWebhookURL().equals("")) {
			DiscordWebhook webhook = new DiscordWebhook(config.getDiscordWebhookURL());
			webhook.setUsername("Loot Announcer Bot");

			DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
			embed.setTitle(item.getName().toUpperCase());
			embed.setColor(Color.ORANGE);
			embed.setThumbnail(getThumbnailURL(item.getID()));
			embed.addField("",
					"Value ・ " + shortenGPValue(item.getGrandExchangePrice()),
					false);

			webhook.addEmbed(embed);
			webhook.execute();
		}
	}

	private String shortenGPValue(int value) {
		String suffix[] = {"", "K", "M", "B"};

		int index = 0;
		while (value / 1000 >= 1) {
			value /= 1000;
			index++;
		}

		DecimalFormat decimalFormat = new DecimalFormat("##.##");
		return String.format("%s%s", decimalFormat.format(value), suffix[index]);
	}
	private String getThumbnailURL(int id) {
		return "https://static.runelite.net/cache/item/icon/" + id + ".png";
	}

	private boolean itemIsValuable(int value) {
		return value >= config.minimumLootValue();
	}

}
