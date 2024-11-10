package com.AerialFishingPearlLuck;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;

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
	private ClientThread clientThread;

	@Inject
	private AerialFishingOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private AerialFishingConfig config; // Configuration variable

	private int fishCaught = 0;
	private int dryStreak; // Will be loaded from config
	private int lastStreak = 0;
	private int tenchProgress = 0;
	private int totalPearls;
	private int sessionPearls;
	private int totalTenches;
	private int bestStreak;
	private int fishCaughtPearlCaught = 0;
	private int sessionFishCaught;
	private double pearlWikiCalc;
	private int levelFishing;
	private int levelHunter;
	private boolean doFetchSkillLevels = true;
	private int totalFishCaught;


	private boolean overlayAdded = false;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Aerial Fishing Tracker started!");


		// Run the code on the client thread to avoid concurrency issues
		clientThread.invoke(() ->
		{
			// Access the equipment container
			ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

			if (equipment != null)
			{
				// Check the weapon slot for an equipped item
				Item weaponItem = equipment.getItem(WEAPON_SLOT);
				if (weaponItem != null && (weaponItem.getId() == ITEM_ID_1 || weaponItem.getId() == ITEM_ID_2))
				{
					overlayManager.add(overlay);
					overlayAdded = true;
					log.debug("Bird equipped. Overlay added.");
				}
			}
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Aerial Fishing Tracker stopped!");
		overlayManager.remove(overlay);
		fishCaught = 0;
		lastStreak = 0;
		tenchProgress = 0;
		fishCaughtPearlCaught = 0;
		sessionPearls = 0;

		overlay.setTenchChanceText("Tench Chance: 0.0%");
	}

	public void updateOverlay()
	{
		double tenchPercentage = (tenchProgress / 20) * 0.1;
		String tenchPercentageFormatted = String.format("%.1f", tenchPercentage);
		overlay.setTenchChanceText("Tench Chance: " + tenchPercentageFormatted + "%");
		log.debug("Current Golden Tench chance: " + tenchPercentageFormatted + "%");
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
			sessionFishCaught++; //adds +1 to the session fish counter to track FishxPearl rate
			totalFishCaught++; // Adds +1 to the total fish caught for people that like to see it
			configManager.setRSProfileConfiguration("pearlluck", "totalFishCaught", totalFishCaught);

			// Used to spoof the golden tench variable for testing
			/*totalTenches++;
			configManager.setRSProfileConfiguration("pearlluck", "totalTench", totalTenches);*/

			updateOverlay();
			log.debug("Overlay Updated");
			log.debug("Fish caught: " + fishCaught + ", Golden Tench progress: " + tenchProgress);
		}

		// Molch Pearl collected
		if (message.equals("<col=ef1020>Untradeable drop: Molch pearl</col>"))
		{
			// Update dryStreak if current fishCaught is greater
			if (fishCaught > dryStreak)
			{
				dryStreak = fishCaught;
				updateOverlay();
				log.debug("Saved dryestStreak to profile: " + totalPearls);
				log.debug("pearl rate" + fishCaughtPearlCaught);

				// Update dryStreak to config for persistence
				configManager.setRSProfileConfiguration("pearlluck", "dryStreak", dryStreak); // add the value to the config dryStreak
				log.debug("Saved dryestStreak to profile: " + dryStreak);
				}

			totalPearls++;
			sessionPearls++;
			configManager.setRSProfileConfiguration("pearlluck", "totalPearls", totalPearls);
			lastStreak = fishCaught; // Sets the last streak value to the fish caught value

			if (bestStreak == 0 || fishCaught < bestStreak)
			{
				bestStreak = fishCaught;
				updateOverlay();
				configManager.setRSProfileConfiguration("pearlluck", "bestStreak", bestStreak); // add the value to the config bestStreak
			}

			if (totalPearls > 0) {
				fishCaughtPearlCaught = sessionFishCaught / sessionPearls;
			} else {
				fishCaughtPearlCaught = 0;
			}

			fishCaught = 0; // Reset the fish count after collecting a Molch Pearl
			log.debug("Molch Pearl collected. Fish count reset.");
		}

		if (message.equals("<col=ef1020>The cormorant has brought you a very strange tench.</col>"))
		{
			totalTenches++;
			configManager.setRSProfileConfiguration("pearlluck", "totalTench", totalTenches);
		}


	}

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
				if (weaponId != ITEM_ID_1 && weaponId != ITEM_ID_2 && overlayAdded)
				{
					overlayManager.remove(overlay);
					overlayAdded = false;
					log.debug("Bird not equipped. Overlay removed.");

					// Reset stats when not using plugin - except dryStreak
					fishCaught = 0;
					lastStreak = 0;
					overlay.setTenchChanceText("Tench Chance: 0.0%");
					log.debug("Values Reset");
				}
				else if ((weaponId == ITEM_ID_1 || weaponId == ITEM_ID_2) && !overlayAdded)
				{
					overlayManager.add(overlay);
					overlayAdded = true;
					log.debug("Bird equipped. Overlay added.");
					loadProfileData();
				}
			}
			else if (overlayAdded)
			{
				overlayManager.remove(overlay);
				overlayAdded = false;
				log.debug("Weapon slot is empty. Overlay removed.");
				fishCaught = 0;
				lastStreak = 0;
				overlay.setTenchChanceText("Tench Chance: 0.0%");
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) throws InterruptedException {
		if (event.getGameState() == GameState.LOGGED_IN) {
			// User has logged in, now we load the profile
			loadProfileData();
		}
	}



	public double pearlRateWikiCalc()
	{
		//log.info("4");
		if (client == null)
		{
			//log.warn("Client not initialized.");
			return -1;
		}

		// Get the users fishing and hunter levels
		//levelFishing = client.getRealSkillLevel(Skill.FISHING);
		//levelHunter = client.getRealSkillLevel(Skill.HUNTER);

		log.debug ("Fishing Level: " + levelFishing);
		log.debug ("Hunter Level: " + levelHunter);

		if (levelFishing == 0 || levelHunter == 0)
		{
			log.debug("Fishing or hunter is 0, which means something's fucked");
			return -1;
		}

		// calculate the X value for the equation
		double X = Math.floor((levelFishing * 2 + levelHunter) / 3.0);
		double intermediateCalculation = (X - 40) * 25 / 59;
		double denominator = 100 - intermediateCalculation;
		pearlWikiCalc = 1 / denominator; // keep as a floating point value

		log.debug("X: " + X);
		log.debug("Intermediate Calculation: " + intermediateCalculation);
		log.debug("Denominator: " + denominator);
		log.debug("Pearl Wiki Calc: " + pearlWikiCalc);
		return pearlWikiCalc;

    }

	@Subscribe
	public void onGameTick(GameTick event)
	{
		//log.info("1");
		if (doFetchSkillLevels)
		{
			//log.info("2");
			if (loadSkillData())
			{
				//log.info("3");
				doFetchSkillLevels = false;
				pearlRateWikiCalc();

			}
		}
	}

	private void loadProfileData()
	{
		// Load dry streak from the config
		Integer savedDryStreak = configManager.getRSProfileConfiguration("pearlluck", "dryStreak", Integer.class);
		Integer savedPearlCount = configManager.getRSProfileConfiguration("pearlluck", "totalPearls", Integer.class);
		Integer savedTenchCount = configManager.getRSProfileConfiguration("pearlluck", "totalTench", Integer.class);
		Integer savedBestStreak = configManager.getRSProfileConfiguration("pearlluck","bestStreak", Integer.class);
		Integer savedTotalFish = configManager.getRSProfileConfiguration("pearlluck", "totalFishCaught", Integer.class);

		// Check if savedDryStreak is null
		if (savedDryStreak == null) {
			dryStreak = -1; // Set to -1 if error
			configManager.setRSProfileConfiguration("pearlluck", "dryStreak", dryStreak); // Save the new value
			log.debug("dryStreak was null. Set to -1.");
		} else {
			dryStreak = savedDryStreak; // Load existing value
			log.debug("Loaded dryStreak from profile: " + dryStreak);

		}

		if (savedTenchCount == null || savedTenchCount < 0) {
			totalTenches = 0; // Default to 0 if not found
			log.debug("totalTench was null. Setting to 0.");
			configManager.setRSProfileConfiguration("pearlluck", "totalTench", totalTenches); // Save the initial value
		} else {
			totalTenches = savedTenchCount;
			log.debug("Loaded totalTench from profile: " + totalTenches);
		}

		totalPearls = savedPearlCount;
		log.debug("Loaded totalPearls from profile: " + totalPearls);
		bestStreak = savedBestStreak;
		totalFishCaught = savedTotalFish;
	}

	private boolean loadSkillData(){
		levelFishing = client.getRealSkillLevel(Skill.FISHING);
		levelHunter = client.getRealSkillLevel(Skill.HUNTER);

		if (levelFishing == 0 || levelHunter == 0)
		{
			log.debug("Fishing or hunter level is 0,retrying next tick.");
			return false;
		}
		log.debug("Loaded fishing and hunter levels");
		return true;
	}

	public String getTenchChanceText()
	{
		final int tenchChance = 40000; // The chance of getting a Golden Tench (1/20000)

		// Calculate the percentage progress towards the next Golden Tench
		double tenchPercentage = (double) tenchProgress / tenchChance * 100;

		// Format the percentage to 1 decimal place
		return String.format("%.1f", tenchPercentage) + "%";
	}

	public AerialFishingConfig getConfig()
	{
		return config; // Add this method to access the config
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

	public double getPearlWikiCalc()
	{
		return pearlWikiCalc;
	}

	public int getSessionFishCaught()
	{
		return sessionFishCaught;
	}

	public int getTotalFishCaught()
	{
		return totalFishCaught;
	}

	public int getDryStreak()
	{
		return dryStreak;
	}

	public int getTotalPearls()
	{
		return totalPearls;
	}

	public int getTotalTenchs()
	{
		return totalTenches;
	}

	public int getLastStreak()
	{
		return lastStreak;
	}

	public int getFishCaughtPearlCaught()
	{
		return fishCaughtPearlCaught;
	}

	public int getBestStreak()
	{
		return bestStreak;
	}

	public int getSessionPearls()
	{
		return sessionPearls;
	}

	public int getTenchChance()
	{
		return tenchProgress;
	}
}