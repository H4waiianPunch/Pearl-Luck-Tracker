package com.AerialFishingPlugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AerialFishingPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AerialFishingPlugin.class);
		RuneLite.main(args);
	}
}
