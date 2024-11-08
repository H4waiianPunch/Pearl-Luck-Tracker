package com.AerialFishingPearlLuck;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("pearlluck")
public interface AerialFishingConfig extends Config
{
	@ConfigItem(
			keyName = "pearlRate",
			name = "Pearl Rate",
			description = "This is the calculated pearl rate that you have. 1/75 to 1/200",
			hidden = false
	)

	//default boolean pearlRate(){return true;}
    default PearlCalcType pearlRate(){
		return PearlCalcType.WikiCalc;
	}

	@ConfigItem(
			keyName = "bestStreak",
			name = "Best Streak",
			description = "The least number of fish caught for a pearl.",
			hidden = false
	)
	default boolean bestStreak() {return true;}

	@ConfigItem(
			keyName = "fishCaught",
			name = "Fish caught",
			description = "Shows the fish caught in this session."
	)
	default boolean fishCaught() {return true;}

	@ConfigItem(
			keyName = "totalFishCaught",
			name = "Total Fish Caught",
			description = "Tracks the total fish caught this session"
	)
	default boolean totalFishCaught() {return true;}

	@ConfigItem(
			keyName = "lastPearl",
			name = "Last Pearl",
			description = "Show the number of fish caught for the last pearl."
	)
	default boolean lastPearl() {return true;}

	@ConfigItem(
			keyName = "showTenchChance",
			name = "Show Tench Chance",
			description = "Every 20 fish caught = .1% (1/20k)"
	)
	default boolean showTenchChance() {
		return true; // Adjust default value as needed
	}

	@ConfigItem(
			keyName = "totalTench",
			name = "Total Golden Tench",
			description = "The total number of Golden Tench you've caught.",
			hidden = false
	)
	default boolean totalTench() {return true;}

	@ConfigItem(
			keyName = "totalPearls",
			name = "Total Pearls",
			description = "This is the total number of pearls gained",
			hidden = false
	)
	default boolean totalPearls() {return true;}

	@ConfigItem(
			keyName = "dryStreak",
			name = "Worst Streak",
			description = "The most number of fish caught for a pearl.",
			hidden = false
	)
	default boolean dryStreak() {return true;}

}

