package com.AerialFishingPearlLuck;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("pearlluck")
public interface AerialFishingConfig extends Config
{
	@ConfigItem(
		keyName = "randommessage",
		name = "Don't hover me please",
		description = "Oh come on, I even asked nicely!"
	)
	default String randommessage()
	{
		return "What are those capybara up to...";
	}

	@ConfigItem(
			keyName = "dryStreak",
			name = "Dryest Streak",
			description = "The most dry you've gone",
			hidden = true
	)
	default int dryStreak()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "showTenchChance",
			name = "Show Tench Chance",
			description = "Check box to HIDE the tench chance."
	)
	default boolean showTenchChance() {
		return true; // Adjust default value as needed
	}
}

