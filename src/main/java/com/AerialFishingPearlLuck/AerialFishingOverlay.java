package com.AerialFishingPearlLuck;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class AerialFishingOverlay extends Overlay
{
    private final AerialFishingPlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();
    private String tenchChanceText = ""; // Add this

    @Inject
    public AerialFishingOverlay(AerialFishingPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT); // Position the overlay at the top left
        setLayer(OverlayLayer.ABOVE_WIDGETS); // The overlay will be drawn above the game widgets
        setPriority(OverlayPriority.HIGH); // High priority ensures it remains visible
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear(); // Clear previous render

        // Add the current number of fish caught to the overlay
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Fish Caught: " + plugin.getFishCaught())
                .color(Color.WHITE) // You can adjust the color
                .build());

        // Add the last number of fish caught before getting a Pearl
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Last Pearl: " + plugin.getLastStreak())
                .color(Color.WHITE) // Color can be adjusted
                .build());

        // Add the highest streak (most fish caught before a Molch Pearl)
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Longest Dry: " + plugin.getDryStreak())
                .color(Color.YELLOW)
                .build());

        // % chance for tench in session
        double tenchPercentage = (double) plugin.getTenchChance() / 40000 * 100; // Had to double the rate here otherwise it was showing up as .1% after 10 fish instead of 20 for some reason.
        String tenchPercentageFormatted = String.format("%.1f", tenchPercentage); // Format it
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Tench Chance: " + tenchPercentageFormatted + "%") // Display the chance
                .color(Color.PINK)
                .build());

        return panelComponent.render(graphics);
    }

    // Add this method to set the Tench chance text
    public void setTenchChanceText(String text)
    {
        this.tenchChanceText = text;
    }
}