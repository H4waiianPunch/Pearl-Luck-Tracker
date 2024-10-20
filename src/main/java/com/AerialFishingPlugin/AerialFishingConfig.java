package com.AerialFishingPlugin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface AerialFishingConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)
	default String greeting()
	{
		return "Hello";
	}

	// Let the dry streak persist through closure
	@ConfigItem(
			keyName = "dryestStreak",
			name = "Dryest Streak",
			description = "Tracks your dry streak through game closure"
	)

	default int dryestStreak()
	{
		return 0;
	}

}
