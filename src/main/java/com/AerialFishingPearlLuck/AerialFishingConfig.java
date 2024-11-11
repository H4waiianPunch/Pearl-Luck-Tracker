package com.AerialFishingPearlLuck;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("pearlluck")
public interface AerialFishingConfig extends Config
{
	/*@ConfigItem(
			keyName = "pearlRate",
			name = "Pearl Rate",
			description = "This is the calculated pearl rate that you have. 1/75 to 1/200",
			hidden = false
	)

	//default boolean pearlRate(){return true;}
    default PearlCalcType pearlRate(){
		return PearlCalcType.WikiCalc;
	}*/

	@ConfigSection(
			name = "Fish Settings",
			description = "All your normal fish settings.",
			position = 0
	)
	String fishCaughtSection = "fishCaughtSection";

	@ConfigSection(
			name = "Pearl Settings",
			description = "All your Molch Pearls settings.",
			position = 1
	)
	String pearlSection = "pearlSection";

	@ConfigSection(
			name = "Tench Settings",
			description = "All your Golden Tench settings.",
			position = 2
	)
	String tenchSection = "tenchSection";

	@ConfigSection(
			name = "Streak Settings",
			description = "All your streak settings.",
			position = 3
	)
	String streakSection = "streakSection";

	@ConfigSection(
			name = "Reset Settings",
			description = "Reset your persistent stats here.",
			position = 4
	)
	String resetSection = "resetSection";

	@ConfigItem(
			keyName = "bestStreak",
			name = "Best Streak",
			description = "The least number of fish caught for a Molch Pearl.",
			section = streakSection
	)
	default boolean bestStreak() {return true;}

	@ConfigItem(
			keyName = "resetBestStreak",
			name = "Reset Best Streak",
			description = "Resets your Best Streak value",
			section = resetSection
	)
	default boolean resetBestStreak() { return false; }

	@ConfigItem(
			keyName = "resetDryStreak",
			name = "Reset Dry Streak",
			description = "Resets your Dry Streak value",
			section = resetSection
	)
	default boolean resetDryStreak() { return false; }

	@ConfigItem(
			keyName = "resetTotalFish",
			name = "Reset Total Fish",
			description = "Resets your Total Fish Caught value",
			section = resetSection
	)
	default boolean resetTotalFish() { return false; }

	@ConfigItem(
			keyName = "resetTotalPearls",
			name = "Reset Total Pearls",
			description = "Resets your Total Molch Pearls value",
			section = resetSection
	)
	default boolean resetTotalPearls() { return false; }

	@ConfigItem(
			keyName = "resetTotalTench",
			name = "Reset Total Tench",
			description = "Resets your Total Golden Tench value",
			section = resetSection
	)
	default boolean resetTotalTench() { return false; }

	@ConfigItem(
			keyName = "fishCaught",
			name = "Fish caught",
			description = "Shows the fish caught until you get a Molch Pearl.",
			section = fishCaughtSection
	)
	default boolean fishCaught() {return true;}


	@ConfigItem(
			keyName = "sessionFishCaught",
			name = "Total Session Fish Caught",
			description = "Tracks the total fish caught this session",
			section = fishCaughtSection
	)
	default boolean sessionFishCaught() {return true;}

	@ConfigItem(
			keyName = "totalFishCaught",
			name = "Total Fish Caught",
			description = "Tracks the lifetime total number of fish caught while Aerial Fishing",
			section = fishCaughtSection
	)
	default boolean totalFishCaught(){return true;}

	@ConfigItem(
			keyName = "lastPearl",
			name = "Last Pearl",
			description = "Show the number of fish caught for the last pearl.",
			section = pearlSection
	)
	default boolean lastPearl() {return true;}

	@ConfigItem(
			keyName = "showTenchChance",
			name = "Show Tench Chance",
			description = "Every 20 fish caught = .1% (1/20k)",
			section = tenchSection
	)
	default boolean showTenchChance() {
		return true; // Adjust default value as needed
	}

	@ConfigItem(
			keyName = "totalTench",
			name = "Total Golden Tench",
			description = "The total number of Golden Tench you've caught.",
			section = tenchSection
	)
	default boolean totalTench() {return true;}

	@ConfigItem(
			keyName = "actualPearlRate",
			name = "Actual Pearl Rate",
			description ="Shows your actual Pearl rate, as calculated by fish caught / pearls caught",
			section = pearlSection
	)
	default boolean actualPearlRate(){return true;}

	@ConfigItem(
			keyName = "wikiPearlRate",
			name = "Wiki Pearl Rate",
			description ="Pearl rate based on the official calculations. 1/75 to 1/200",
			section = pearlSection
	)
	default boolean wikiPearlRate(){return true;}

	@ConfigItem(
			keyName = "totalPearls",
			name = "Total Pearls",
			description = "This is the total number of pearls gained",
			section = pearlSection
	)
	default boolean totalPearls() {return true;}

	@ConfigItem(
			keyName = "sessionPearls",
			name = "Session Pearls",
			description = "Enable to see the total pearls caught this session",
			section = pearlSection
	)

	default boolean sessionPearls() {return true;}


	@ConfigItem(
			keyName = "dryStreak",
			name = "Worst Streak",
			description = "The most number of fish caught for a Molch Pearl.",
			section = streakSection
	)
	default boolean dryStreak() {return true;}

}

