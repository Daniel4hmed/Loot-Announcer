package com.lootannouncer;

import com.lootannouncer.discord.DiscordWebhook;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
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

	private static final String OWNED_PET_MESSAGE = "You have a funny feeling like you would have been followed";
	private static final ImmutableList<String> PET_MESSAGES = ImmutableList.of(
			"You have a funny feeling like you're being followed",
			"You feel something weird sneaking into your backpack",
			"You have a funny feeling like you would have been followed");

	@Provides
	LootAnnouncerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(LootAnnouncerConfig.class);
	}

	@Override
	protected void startUp() { }

	@Override
	protected void shutDown() {	}

	@Subscribe
	public void onChatMessage(ChatMessage messageEvent) {

		if (messageEvent.getType() != ChatMessageType.GAMEMESSAGE) return;
		if (!config.includePetDrops()) return;

		String chatMessage = messageEvent.getMessage();

		if (PET_MESSAGES.stream().anyMatch(chatMessage::contains)) {
			boolean duplicate = chatMessage.equals(OWNED_PET_MESSAGE);
			try {
				sendPetAnnouncement(duplicate);
			} catch (IOException e) {
				throw new RuntimeException();
			}
		}
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

	private void sendPetAnnouncement(boolean duplicate) throws IOException {
		// Check if Webhook URL is not empty
		if (!config.getDiscordWebhookURL().equals("")) {

			// Use Kitten Thumbnail
			final int PET_THUMBNAIL_ID = 1556;
			String title = duplicate ? "The duplicate pet ran off..." : "You just received a pet!";
			String description = duplicate ? "The pet saw its cousin and ran away! (You already own this pet)" : "Congratulations on your new pet!";

			DiscordWebhook webhook = new DiscordWebhook(config.getDiscordWebhookURL());
			webhook.setUsername("Loot Announcer Bot");

			DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
			embed.setTitle(title);
			embed.setColor(Color.ORANGE);
			embed.setThumbnail(getThumbnailURL(PET_THUMBNAIL_ID));
			embed.setDescription(description);

			webhook.addEmbed(embed);
			webhook.execute();
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
					"Value: " + shortenGPValue(item.getGrandExchangePrice()),
					false);

			webhook.addEmbed(embed);
			webhook.execute();
		}
	}

	private String shortenGPValue(float value) {
		String[] suffix = {"", "K", "M", "B"};

		int index = 0;
		while (value / 1000 >= 1) {
			value /= 1000;
			index++;
		}

		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		return String.format("%s%s", decimalFormat.format(value), suffix[index]);
	}
	private String getThumbnailURL(int id) {
		return "https://static.runelite.net/cache/item/icon/" + id + ".png";
	}

	private boolean itemIsValuable(int value) {
		return value >= config.minimumLootValue();
	}

}