package com.AerialFishingPearlLuck;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

import javax.inject.Inject;
import java.awt.*;

public class AerialFishingOverlay extends Overlay
{
    private final AerialFishingPlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public AerialFishingOverlay(AerialFishingPlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT); // Position the overlay at the top left
        setLayer(OverlayLayer.ABOVE_WIDGETS); // The overlay will be drawn above the game widgets
        setPriority(OverlayPriority.HIGH); // High priority ensures it remains visible

        // The following addMenuEntries will allow the persistent stats to be reset via a shift+click menu on the overlay.
        // They don't seem to need a "target" but the field needs to be there otherwise it breaks.

        addMenuEntry(RUNELITE_OVERLAY, "Reset Total Pearls", "", e -> plugin.resetTotalPearls());
        addMenuEntry(RUNELITE_OVERLAY, "Reset Total Fish Caught", "", e -> plugin.resetTotalFishCaught());
        addMenuEntry(RUNELITE_OVERLAY, "Reset Total Tenches Caught", "", e -> plugin.resetTotalTench());
        addMenuEntry(RUNELITE_OVERLAY, "Reset Dry Streak", "", e -> plugin.resetDryStreak());
        addMenuEntry(RUNELITE_OVERLAY, "Reset Best Streak", "", e -> plugin.resetBestStreak());
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear(); // Clear previous render

        if (plugin.isTotalFishCaughtEnabled()){
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Total Fish: " + plugin.getTotalFishCaught())
                    .color(Color.WHITE)
                    .build());
        }

        if (plugin.isSessionFishCaughtEnabled()){
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Session Fish: " + plugin.getSessionFishCaught())
                    .color(Color.WHITE)
                    .build());
        }

        // Add the current number of fish caught to the overlay
        if (plugin.isFishCaughtEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Fish Caught: " + plugin.getFishCaught())
                    .color(Color.WHITE) // You can adjust the color
                    .build());
        }

        // Add the last number of fish caught before getting a Pearl
        if (plugin.isLastPearlEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Last Pearl: " + plugin.getLastStreak())
                    .color(Color.CYAN) // Color can be adjusted
                    .build());
        }

        if (plugin.isSessionPearlsEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Session Pearls: " + plugin.getSessionPearls())
                    .color(Color.CYAN)
                    .build());
        }

        if (plugin.isTotalPearlsEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Total Pearls: " + plugin.getTotalPearls())
                    .color(Color.CYAN)
                    .build());
        }

        if (plugin.isActualPearlRateEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Actual Rate: 1/" + plugin.getFishCaughtPearlCaught())
                    .color(Color.CYAN)
                    .build());
        }

        if (plugin.isWikiPearlRateEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Wiki Rate: 1/" + (int) Math.floor(1 / plugin.getPearlWikiCalc()))
                    .color(Color.CYAN)
                    .build());
        }

        // Add the highest streak (most fish caught before a Molch Pearl)
        if (plugin.isDryStreakEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Longest Dry: " + plugin.getDryStreak())
                    .color(Color.YELLOW)
                    .build());
        }

        if (plugin.isBestStreakEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Biggest Spoon: " + plugin.getBestStreak())
                    .color(Color.YELLOW)
                    .build());
    }
        if (plugin.isTotalTenchEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Total Tench: " + plugin.getTotalTenchs())
                    .color(Color.PINK)
                    .build());
        }

        if (plugin.isShowTenchChanceEnabled()) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Tench Chance: " + plugin.getGoldenTenchChance() + "%")
                    .color(Color.PINK)
                    .build());
        }



        return panelComponent.render(graphics);
    }
    public void updateOverlay()
    {

    }
}