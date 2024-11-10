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
    private String tenchChanceText = "Tench Chance: 0.0%"; // Starts the Tench Chance at 0.0%

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

        if (plugin.getConfig().totalFishCaught()){
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Total Fish: " + plugin.getTotalFishCaught())
                    .color(Color.WHITE)
                    .build());
        }

        // Add the current number of fish caught to the overlay
        if (plugin.getConfig().fishCaught()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Fish Caught: " + plugin.getFishCaught())
                    .color(Color.WHITE) // You can adjust the color
                    .build());
        }

        // Add the last number of fish caught before getting a Pearl
        if (plugin.getConfig().lastPearl()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Last Pearl: " + plugin.getLastStreak())
                    .color(Color.LIGHT_GRAY) // Color can be adjusted
                    .build());
        }

        if (plugin.getConfig().actualPearlRate()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Actual Rate: 1/" + plugin.getFishCaughtPearlCaught())
                    .color(Color.LIGHT_GRAY)
                    .build());
        }

        if (plugin.getConfig().wikiPearlRate()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Wiki Rate: 1/" + plugin.pearlRateWikiCalc())
                    .color(Color.LIGHT_GRAY)
                    .build());
        }

        /*switch (plugin.getConfig().pearlRate()) {
            case None:
                panelComponent.getChildren().remove(TitleComponent.builder()
                        .text("FishCaught/PearlsGained") //idk why this needs to be here still
                        .color(Color.LIGHT_GRAY) //idk why this needs to be here still
                        .build()); //idk why this needs to be here still
                break;

            case FishCaughtPearlsGained:
                panelComponent.getChildren().add(TitleComponent.builder()
                        .text("Pearl Rate: 1/" + plugin.getFishCaughtPearlCaught())
                        .color(Color.LIGHT_GRAY)
                        .build());
                break;

            case WikiCalc:
                double pearlRate = plugin.getPearlWikiCalc();
                if (pearlRate != -1) {
                    long roundedRate = Math.round(1 / pearlRate);
                    panelComponent.getChildren().add(TitleComponent.builder()
                            .text("Pearl Rate: " + roundedRate)
                            .color(Color.LIGHT_GRAY)
                            .build());
                    break;
                }
        }*/

        if (plugin.getConfig().sessionPearls()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Session Pearls: " + plugin.getSessionPearls())
                    .color(Color.LIGHT_GRAY)
                    .build());
        }

        if (plugin.getConfig().totalPearls()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Total Pearls: " + plugin.getTotalPearls())
                    .color(Color.LIGHT_GRAY)
                    .build());
        }

        // Add the highest streak (most fish caught before a Molch Pearl)
        if (plugin.getConfig().dryStreak()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Longest Dry: " + plugin.getDryStreak())
                    .color(Color.YELLOW)
                    .build());
        }

        if (plugin.getConfig().bestStreak()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Biggest Spoon: " + plugin.getBestStreak())
                    .color(Color.YELLOW)
                    .build());
    }
        if (plugin.getConfig().totalTench()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Total Tench: " + plugin.getTotalTenchs())
                    .color(Color.PINK)
                    .build());
        }



        // % chance for tench in session
        if (plugin.getConfig().showTenchChance()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text(tenchChanceText)
                    .color(Color.PINK)
                    .build());
        }



        return panelComponent.render(graphics);
    }

    // Add this method to set the Tench chance text
    public void setTenchChanceText(String text)
    {
        this.tenchChanceText = text;
    }

    public void updateOverlay()
    {

    }
}