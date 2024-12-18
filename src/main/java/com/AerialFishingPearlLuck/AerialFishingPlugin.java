package com.AerialFishingPearlLuck;
// VERSION 1.2


import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import java.text.DecimalFormat;

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

    // Add this method to access the config
    @Getter
    @Inject
	private AerialFishingConfig config; // Configuration variable

	@Getter
    private int fishCaught = 0;
	@Getter
    private int dryStreak; // Will be loaded from config
	@Getter
    private int lastStreak = 0;
	private int tenchProgress = 0;
	@Getter
    private int totalPearls;
	@Getter
    private int sessionPearls;
	private int totalTenches;
	@Getter
    private int bestStreak;
	@Getter
    private int fishCaughtPearlCaught = 0;
	@Getter
    private int sessionFishCaught;
	@Getter
    private double pearlWikiCalc;
	private int levelFishing;
	private int levelHunter;
	private boolean doFetchSkillLevels = true;
	@Getter
    private int totalFishCaught;
	private double tenchChance;

	private boolean overlayAdded = false;
	@Getter
    private boolean totalFishCaughtEnabled = false;
	@Getter
    private boolean sessionFishCaughtEnabled = false;
	@Getter
    private boolean fishCaughtEnabled = false;
	@Getter
    private boolean lastPearlEnabled = false;
	@Getter
    private boolean sessionPearlsEnabled = false;
	@Getter
    private boolean totalPearlsEnabled = false;
	@Getter
    private boolean actualPearlRateEnabled = false;
	@Getter
    private boolean wikiPearlRateEnabled = false;
	@Getter
    private boolean dryStreakEnabled = false;
	@Getter
    private boolean bestStreakEnabled = false;
	@Getter
    private boolean totalTenchEnabled = false;
	@Getter
    private boolean showTenchChanceEnabled = false;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Aerial Fishing Tracker started!");


		// Honestly don't know what this does
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

		// Loading the configs to see if they're enabled or not
			totalFishCaughtEnabled = getConfig().totalFishCaught();
			sessionFishCaughtEnabled = getConfig().sessionFishCaught();
			fishCaughtEnabled = getConfig().fishCaught();
			lastPearlEnabled = getConfig().lastPearl();
			sessionPearlsEnabled = getConfig().sessionPearls();
			totalPearlsEnabled = getConfig().totalPearls();
			actualPearlRateEnabled = getConfig().actualPearlRate();
			wikiPearlRateEnabled = getConfig().wikiPearlRate();
			dryStreakEnabled = getConfig().dryStreak();
			bestStreakEnabled = getConfig().bestStreak();
			totalTenchEnabled = getConfig().totalTench();
			showTenchChanceEnabled = getConfig().showTenchChance();
	}

	@Override
	protected void shutDown() throws Exception // Resets a bunch of stats on shutdown
	{
		log.debug("Aerial Fishing Tracker stopped!");
		overlayManager.remove(overlay);
		fishCaught = 0;
		lastStreak = 0;
		tenchProgress = 0;
		fishCaughtPearlCaught = 0;
		sessionPearls = 0;
		sessionFishCaught = 0;
		tenchChance = 0;
		configManager.setRSProfileConfiguration("pearlluck", "totalFishCaught", totalFishCaught); // Updates the totalFishCaught calc for persistence. It was decided that updating per fish caught was too much.
	}

	@Subscribe
	protected void onConfigChanged(ConfigChanged configChanged) // This will allow the overlay values to change with the new way of tracking what's active.
	{
		if (configChanged.getGroup().equals("pearlluck"))
		{
			readConfig();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		String message = event.getMessage();

		// Cormorant catches a fish chat message
		if (message.equals("You send your cormorant to try to catch a fish from out at sea."))
		{
			fishCaught++; // add +1 to the counter
			tenchProgress++; // add +1 to the fish caught towards golden tench
			sessionFishCaught++; //adds +1 to the session fish counter to track FishxPearl rate
			totalFishCaught++; // Adds +1 to the total fish caught for people that like to see it
			tenchChance = Double.parseDouble(calculateGoldenTenchChance()); //Gets the tench chance from function below

			// Used to spoof the golden tench variable for testing
			/*totalTenches++;
			configManager.setRSProfileConfiguration("pearlluck", "totalTench", totalTenches);*/

			log.debug("Fish caught: " + fishCaught + ", Golden Tench progress: " + tenchProgress);
		}

		// Molch Pearl collected
		if (message.equals("<col=ef1020>Untradeable drop: Molch pearl</col>"))
		{
			// Update dryStreak if current fishCaught is greater
			if (fishCaught > dryStreak)
			{
				dryStreak = fishCaught;
				//updateOverlay();
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
			tenchChance = 0;
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

					// Reset stats
					fishCaught = 0;
					lastStreak = 0;
					sessionFishCaught = 0;
					sessionPearls = 0;
					tenchChance = 0;
					configManager.setRSProfileConfiguration("pearlluck", "totalFishCaught", totalFishCaught); // Updates the totalFishCaught calc for persistence
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
				sessionFishCaught = 0;
				sessionPearls = 0;
				tenchChance = 0;
				configManager.setRSProfileConfiguration("pearlluck", "totalFishCaught", totalFishCaught);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) throws InterruptedException {
		if (event.getGameState() == GameState.LOGGED_IN) {
			// User has logged in, now we load the profile
			loadProfileData();
		}
		//log.info("Gamestate: " + event.getGameState());

		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING){
			configManager.setRSProfileConfiguration("pearlluck", "totalFishCaught", totalFishCaught);
		}
	}



	@Subscribe
	public void onStatChanged(StatChanged event) { 
		Skill skill = event.getSkill();

		//Only call loadSkillData if the level is fishing or hunter
		if (overlayAdded && skill == Skill.FISHING || skill == Skill.HUNTER) {
			doFetchSkillLevels = true;
		}
	}

	public double pearlRateWikiCalc()
	{
		if (client == null)
		{
			return -1;
		}

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

	private String calculateGoldenTenchChance() {
		if (sessionFishCaught <= 0) {
			log.debug("Tench chance: 0.0 (no fish caught yet)");
			return "0.0";}

		double dropRate = 1.0 / 20000.0;
		double noTenchProbability = 1 - dropRate;
		double tenchChance = 1 - Math.pow(noTenchProbability, sessionFishCaught);

		DecimalFormat df = new DecimalFormat("##.###");
		String formattedTenchChance = df.format(tenchChance*100);

		log.debug("Calculated Tench chance with " + sessionFishCaught + " fish caught: " + (tenchChance * 100));

		return formattedTenchChance;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (doFetchSkillLevels)
		{
			if (loadSkillData())
			{
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

	private void readConfig()
	{
		totalFishCaughtEnabled = config.totalFishCaught();
		sessionFishCaughtEnabled = config.sessionFishCaught();
		fishCaughtEnabled = config.fishCaught();
		lastPearlEnabled = config.lastPearl();
		sessionPearlsEnabled = config.sessionPearls();
		totalPearlsEnabled = config.totalPearls();
		actualPearlRateEnabled = config.actualPearlRate();
		wikiPearlRateEnabled = config.wikiPearlRate();
		dryStreakEnabled = config.dryStreak();
		bestStreakEnabled = config.bestStreak();
		totalTenchEnabled = config.totalTench();
		showTenchChanceEnabled = config.showTenchChance();
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

	public void resetTotalPearls() { // Resets this stat from the shift + click dropdown from overlay class
		totalPearls = 0;
		configManager.setRSProfileConfiguration("pearlluck", "totalPearls", totalPearls);
	}

	public void resetTotalFishCaught() { // Resets this stat from the shift + click dropdown from overlay class
		totalFishCaught = 0;
		configManager.setRSProfileConfiguration("pearlluck", "totalFishCaught", totalFishCaught);
	}

	public void resetTotalTench() { // Resets this stat from the shift + click dropdown from overlay class
		totalTenches = 0;
		configManager.setRSProfileConfiguration("pearlluck", "totalTench", totalTenches);
	}

	public void resetDryStreak() { // Resets this stat from the shift + click dropdown from overlay class
		dryStreak = 0;
		configManager.setRSProfileConfiguration("pearlluck", "dryStreak", dryStreak);
	}

	public void resetBestStreak() { // Resets this stat from the shift + click dropdown from overlay class
		bestStreak = 0;
		configManager.setRSProfileConfiguration("pearlluck", "bestStreak", bestStreak);
	}


    @Provides
	AerialFishingConfig provideConfig()
	{
		return configManager.getConfig(AerialFishingConfig.class);
	}

    public double getGoldenTenchChance()
	{
		return tenchChance;
	}

    public int getTotalTenchs()
	{
		return totalTenches;
	}

}