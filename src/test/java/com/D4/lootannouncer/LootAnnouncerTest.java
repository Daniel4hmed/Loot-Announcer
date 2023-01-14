package com.D4.lootannouncer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LootAnnouncerTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LootAnnouncerPlugin.class);
		RuneLite.main(args);
	}
}