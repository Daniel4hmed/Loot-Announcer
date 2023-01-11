package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Loot Announcer"
)
public class LootAnnouncer extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private LootAnnouncerConfig config;

	private static final int COINS = ItemID.COINS_995;

	@Provides
	LootAnnouncerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(LootAnnouncerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Loot Announcer started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Loot Announcer stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
		}
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
			logValuableItem(item);
		}
	}

	private boolean itemIsValuable(int value) {
		return value >= config.lootValue() ? true : false;
	}

	private void logValuableItem(Item item) {
		log.info("--- [ Expensive Drop ] ---");
		log.info("Name: " + item.getName());
		log.info(("GE Value: " + item.getGrandExchangePrice()));
	}
}
