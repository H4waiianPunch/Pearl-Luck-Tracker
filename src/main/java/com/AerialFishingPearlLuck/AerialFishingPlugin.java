package com.AerialFishingPearlLuck;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
		name = "Aerial Fishing Pearl Luck"
)
public class AerialFishingPlugin extends Plugin
{
	private static final int WEAPON_SLOT = 3; // The equipment slot index for the weapon
	private static final int ITEM_ID_1 = 22817;
	private static final int ITEM_ID_2 = 22816;

	@Inject
	private Client client;

	@Inject
	private AerialFishingOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	private AerialFishingConfig config; // Configuration variable

	private int fishCaught = 0;
	private int dryStreak; // Will be loaded from config
	private int lastStreak = 0;
	private int tenchProgress = 0;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Aerial Fishing Tracker started!");
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Aerial Fishing Tracker stopped!");
		overlayManager.remove(overlay);

		/*// Save the dryest streak before shutdown
		log.info("Saving dryStreak: " + dryStreak + " to profile.");
		configManager.setRSProfileConfiguration("pearlluck", "dryStreak", dryStreak);
		log.info("Saved dryStreak to profile: " + dryStreak);*/

		fishCaught = 0;
		lastStreak = 0;

		//log.info("Saving dryStreak to profile. Current value to save: " + dryStreak);

	}

	public void updateOverlay()
	{
		// Assuming tenchProgress is the number of fish caught since starting to track Golden Tench
		// The chance for Golden Tench is 1/20,000

		final int tenchChance = 40000; // The chance of getting a Golden Tench (1/20000)

		// Calculate the percentage progress towards the next Golden Tench
		log.info("Tench Progress: " + tenchProgress + ", Tench Chance: " + tenchChance);

		double tenchPercentage = (double) tenchProgress / tenchChance * 100;

		// Format the percentage to 1 decimal place
		String tenchPercentageFormatted = String.format("%.1f", tenchPercentage);

		// Now, you can display this percentage in the overlay
		overlay.setTenchChanceText("Tench Chance: " + tenchPercentageFormatted + "%");

		log.info("Current Golden Tench chance: " + tenchPercentageFormatted + "%");
	}


	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		String message = event.getMessage();

		// Comoront catches a fish chat message
		if (message.equals("You send your cormorant to try to catch a fish from out at sea."))
		{
			fishCaught++; // add +1 to the counter
			tenchProgress++; // add +1 to the fish caught towards golden tench
			log.info("Fish caught: " + fishCaught + ", Golden Tench progress: " + tenchProgress);


		}

		// Molch Pearl collected
		if (message.equals("<col=ef1020>Untradeable drop: Molch pearl</col>"))
		{
			// Update dryStreak if current fishCaught is greater
			if (fishCaught > dryStreak)
			{
				dryStreak = fishCaught;

				// Update dryStreak to config for persistence
				configManager.setRSProfileConfiguration("pearlluck", "dryStreak", dryStreak); // add the value to the config dryStreak
				log.info("Saved dryestStreak to profile: " + dryStreak);
			}
			lastStreak = fishCaught; // Sets the last streak value to the fish caught value

			fishCaught = 0; // Reset the fish count after collecting a Molch Pearl
			log.info("Molch Pearl collected. Fish count reset.");
		}
	}

	// This is just to show a message that the plugin has started. Not required, but a good reference.
	//@Subscribe
	//public void onGameStateChanged(GameStateChanged gameStateChanged)
	//{
	//	if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
	//	{
	//		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Aerial Fishing Tracker is active!", null);
	//	}
	//}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		// Check if the container is the equipment container
		if (event.getContainerId() == InventoryID.EQUIPMENT.getId())
		{
			ItemContainer equipment = event.getItemContainer();
			Item weaponItem = equipment.getItem(WEAPON_SLOT);

			if (weaponItem != null)
			{
				int weaponId = weaponItem.getId();

				// If the item in the weapon slot is neither ITEM_ID_1 nor ITEM_ID_2, remove the overlay
				if (weaponId != ITEM_ID_1 && weaponId != ITEM_ID_2)
				{
					overlayManager.remove(overlay);
					log.info("Bird not equipped. Overlay removed.");

					// Reset stats when not using plugin - except dryStreak
					fishCaught = 0;
					lastStreak = 0;
					log.info("Values Reset");
				}
				else
				{
					overlayManager.add(overlay);
					log.info("Bird equipped. Overlay added.");
					Integer savedDryStreak = configManager.getRSProfileConfiguration("pearlluck", "dryStreak", Integer.class);
				}
			}
			else
			{
				// No item in weapon slot
				overlayManager.remove(overlay);
				log.info("Weapon slot is empty. Overlay removed.");

				fishCaught = 0;
				lastStreak = 0;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			// User has logged in, now we load the profile
			loadProfileData();
		}
	}

	private void loadProfileData()
	{
		// Load dry streak from the config
		Integer savedDryStreak = configManager.getRSProfileConfiguration("pearlluck", "dryStreak", Integer.class);

		// Check if savedDryStreak is null
		if (savedDryStreak == null) {
			dryStreak = -1; // Set to -1 if error
			configManager.setRSProfileConfiguration("pearlluck", "dryStreak", dryStreak); // Save the new value
			log.info("dryStreak was null. Set to -1.");
		} else {
			dryStreak = savedDryStreak; // Load existing value
			log.info("Loaded dryStreak from profile: " + dryStreak);
		}
	}

	public String getTenchChanceText()
	{
		final int tenchChance = 40000; // The chance of getting a Golden Tench (1/20000)

		// Calculate the percentage progress towards the next Golden Tench
		double tenchPercentage = (double) tenchProgress / tenchChance * 100;

		// Format the percentage to 1 decimal place
		return String.format("%.1f", tenchPercentage) + "%";
	}


	@Provides
	AerialFishingConfig provideConfig()
	{
		return configManager.getConfig(AerialFishingConfig.class);
	}

	public int getFishCaught()
	{
		return fishCaught;
	}

	public int getDryStreak()
	{
		return dryStreak;
	}

	public int getLastStreak()
	{
		return lastStreak;
	}

	public int getTenchChance()
	{
		return tenchProgress;
	}
}