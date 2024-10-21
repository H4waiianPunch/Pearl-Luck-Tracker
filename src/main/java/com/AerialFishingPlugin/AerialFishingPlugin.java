package com.AerialFishingPlugin;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
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
		name = "Aerial Fishing Tracker"
)
public class AerialFishingPlugin extends Plugin
{
	private static final int WEAPON_SLOT = 3; // The equipment slot index for the weapon
	private static final int ITEM_ID_1 = 22817; // Replace with actual item ID 1
	private static final int ITEM_ID_2 = 22816; // Replace with actual item ID 2

	@Inject
	private Client client;

	@Inject
	private AerialFishingOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager; // Ensure this is injected

	private AerialFishingConfig config; // Configuration variable

	private int fishCaught = 0;
	private int dryestStreak; // Will be loaded from config
	private int lastStreak = 0;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Aerial Fishing Tracker started!");
		overlayManager.add(overlay);

		// Load dry streak from the config
		Integer savedDryestStreak = configManager.getRSProfileConfiguration("example", "dryestStreak", Integer.class);
		dryestStreak = savedDryestStreak != null ? savedDryestStreak : 0;
		log.info("Loaded dryestStreak from profile" + dryestStreak);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Aerial Fishing Tracker stopped!");
		overlayManager.remove(overlay);

		// Save the dryest streak before shutdown
		configManager.setRSProfileConfiguration("example", "dryestStreak", dryestStreak);
		log.info("Saved dryestStreak to profile: " + dryestStreak);

		fishCaught = 0;
		lastStreak = 0;

	}



	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		String message = event.getMessage();

		// Detect the message when the cormorant is sent to catch a fish
		if (message.equals("You send your cormorant to try to catch a fish from out at sea."))
		{
			fishCaught++;
			log.info("Fish caught: " + fishCaught);
		}

		// Detect the message when a Molch Pearl is collected
		if (message.equals("<col=ef1020>Untradeable drop: Molch pearl</col>"))
		{
			// Update highest streak if current fishCaught is greater
			if (fishCaught > dryestStreak)
			{
				dryestStreak = fishCaught;

				// Update dryestStreak to config for persistence
				configManager.setRSProfileConfiguration("example", "dryestStreak", dryestStreak);
				log.info("Saved dryestStreak to profile: " + dryestStreak);
				log.info("New highest dryestStreak: " + dryestStreak);
			}
			lastStreak = fishCaught; // Sets the last streak value to the fish caught value

			fishCaught = 0; // Reset the fish count after collecting a Molch Pearl
			log.info("Molch Pearl collected. Fish count reset.");
		}
	}

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

					// Reset stats when not using plugin - except dryest streak
					fishCaught = 0;
					lastStreak = 0;
					log.info("Values Reset");
				}
				else
				{
					overlayManager.add(overlay);
					log.info("Bird equipped. Overlay added.");

					// Pull the dry streak into the overlay from config
					//configManager.getRSProfileConfiguration("example", "dryestStreak", Integer.class);
					//log.info("Saved dryestStreak to profile: " + dryestStreak);


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

	@Provides
	AerialFishingConfig provideConfig()
	{
		return configManager.getConfig(AerialFishingConfig.class);
	}

	public int getFishCaught()
	{
		return fishCaught;
	}

	public int getDryestStreak()
	{
		return dryestStreak;
	}

	public int getLastStreak()
	{
		return lastStreak;
	}
}