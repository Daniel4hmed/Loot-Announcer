package com.example;

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

import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

		// Template of item by ID
		ItemComposition itemComposition = itemManager.getItemComposition(itemID);

		// Checks if item is noted and returns correct ID if it is
		final int realItemID = itemComposition.getNote() != -1 ? itemComposition.getLinkedNoteId() : itemID;

		// Create Item
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

			// Format the content
			String content = String.format("[ Expensive Loot Dropped ] \n" +
					"Item: %s\n" + "GE Price: %s",
					item.getName(), item.getGrandExchangePrice()
			);

			// Make JSONObject for Webhook
			JSONObject json = new JSONObject();
			json.put("content", content);
			json.put("username", "Test");

			openWebhookConnection();
			sendJSONToWebhook(json);
			closeWebhookConnection();
		}
	}

	private void openWebhookConnection() throws IOException {
		// Connects to the Webhook URL
		URL webhookURL = new URL(config.getDiscordWebhookURL());
		connection = (HttpURLConnection) webhookURL.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0");
		connection.addRequestProperty("Content-Type", "application/json");
		connection.setDoOutput(true);
	}

	private void closeWebhookConnection() throws IOException {
		connection.getInputStream().close();
		connection.disconnect();
	}

	private void sendJSONToWebhook(JSONObject json) throws IOException {
		OutputStream stream = connection.getOutputStream();
		stream.write(json.toString().getBytes());
		stream.flush();
		stream.close();
	}

	private boolean itemIsValuable(int value) {
		return value >= config.minimumLootValue();
	}

}
