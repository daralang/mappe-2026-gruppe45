package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PlayerStatsService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Persistent footer bar shown at the bottom of {@link edu.ntnu.idatt2003.millions.view.MainView}.
 * Displays the player's name and status progress on the left, and equity / available
 * funds on the right. Updates whenever the game state or active language changes.
 */
public class StatusFooter extends HBox implements GameObserver {

    private final GameService gameService;
    private final PlayerStatsService statsService = new PlayerStatsService();

    // Drives fill width reactively — updated via progressProp.set() in refresh()
    private final DoubleProperty progressProp = new SimpleDoubleProperty(0.0);

    private final Label playerLabel = new Label();
    private final Label playerValue = new Label();
    private final Label equityLabel = new Label();
    private final Label equityValue = new Label();
    private final Label availableLabel = new Label();
    private final Label availableValue = new Label();

    // Status row nodes — built once, kept alive, visibility-toggled between modes
    private final Label leftStatusLabel = new Label();
    private final Label rightStatusLabel = new Label();
    private final Region fill = new Region();
    private HBox progressRow;
    private HBox speculatorBadge;
    private Label speculatorLabel;

    public StatusFooter(GameService gameService) {
        this.gameService = gameService;
        getStyleClass().add("status-footer");
        gameService.addObserver(this);
        LanguageManager.addObserver(this::onLanguageChanged);
        buildLayout();
        onLanguageChanged();
    }

    private void buildLayout() {
        // Player name group
        playerLabel.getStyleClass().add("status-footer-label");
        playerValue.getStyleClass().add("status-footer-value");
        HBox playerGroup = new HBox(10, playerLabel, playerValue);
        playerGroup.setAlignment(Pos.CENTER_LEFT);

        // Progress bar — track fills the StackPane, fill is capped at progress fraction
        leftStatusLabel.getStyleClass().add("status-footer-label");
        rightStatusLabel.getStyleClass().add("status-footer-label");

        Region track = new Region();
        track.getStyleClass().add("status-footer-progress-track");
        track.setMaxWidth(Double.MAX_VALUE);

        fill.getStyleClass().addAll("status-footer-progress-fill", "status-footer-progress-fill-novice");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(progressProp));
        fill.maxWidthProperty().bind(track.widthProperty().multiply(progressProp));
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        StackPane barContainer = new StackPane(track, fill);
        barContainer.setMinWidth(120);
        HBox.setHgrow(barContainer, Priority.ALWAYS);

        progressRow = new HBox(8, leftStatusLabel, barContainer, rightStatusLabel);
        progressRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(progressRow, Priority.ALWAYS);

        // Speculator badge — hidden by default, shown when SPECULATOR
        Label crown = new Label("♔");
        crown.getStyleClass().add("status-footer-speculator-icon");
        speculatorLabel = new Label();
        speculatorLabel.getStyleClass().add("status-footer-speculator-label");
        speculatorBadge = new HBox(8, crown, speculatorLabel);
        speculatorBadge.getStyleClass().add("status-footer-speculator-badge");
        speculatorBadge.setAlignment(Pos.CENTER_LEFT);
        speculatorBadge.setVisible(false);
        speculatorBadge.setManaged(false);

        // Left side: player group + whichever status widget is active
        HBox leftSide = new HBox(38, playerGroup, progressRow, speculatorBadge);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region divider = new Region();
        divider.getStyleClass().add("status-footer-divider");

        equityLabel.getStyleClass().add("status-footer-label");
        equityValue.getStyleClass().add("status-footer-value");
        HBox equityGroup = new HBox(10, equityLabel, equityValue);
        equityGroup.setAlignment(Pos.CENTER_LEFT);

        availableLabel.getStyleClass().add("status-footer-label");
        availableValue.getStyleClass().add("status-footer-value");
        HBox availableGroup = new HBox(10, availableLabel, availableValue);
        availableGroup.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(leftSide, spacer, divider, equityGroup, availableGroup);
    }

    private void onLanguageChanged() {
        playerLabel.setText(LanguageManager.get("footer.player"));
        equityLabel.setText(LanguageManager.get("footer.equity"));
        availableLabel.setText(LanguageManager.get("footer.available"));
        refresh();
    }

    private void refresh() {
        Player player = gameService.getPlayer();
        CurrencyConverter converter = gameService.getCurrencyConverter();

        playerValue.setText(player.getName());
        equityValue.setText(CurrencyFormatter.format(statsService.getNetWorth(player, converter)));
        availableValue.setText(CurrencyFormatter.format(player.getCash()));

        PlayerStatusLevel status = statsService.getStatus(player, converter);
        double progress = statsService.getProgressToNextStatus(player, converter);
        renderStatusRow(status, progress);
    }

    private void renderStatusRow(PlayerStatusLevel status, double progress) {
        progressProp.set(progress);

        if (status == PlayerStatusLevel.SPECULATOR) {
            progressRow.setVisible(false);
            progressRow.setManaged(false);
            speculatorBadge.setVisible(true);
            speculatorBadge.setManaged(true);
            speculatorLabel.setText(LanguageManager.get("status.speculator").toUpperCase());
        } else {
            speculatorBadge.setVisible(false);
            speculatorBadge.setManaged(false);
            progressRow.setVisible(true);
            progressRow.setManaged(true);

            leftStatusLabel.setText(LanguageManager.get(
                    status == PlayerStatusLevel.NOVICE ? "status.novice" : "status.investor"));
            rightStatusLabel.setText(LanguageManager.get(
                    status == PlayerStatusLevel.NOVICE ? "status.investor" : "status.speculator"));

            String fillClass = status == PlayerStatusLevel.NOVICE
                    ? "status-footer-progress-fill-novice"
                    : "status-footer-progress-fill-investor";
            fill.getStyleClass().removeIf(c ->
                    c.equals("status-footer-progress-fill-novice")
                    || c.equals("status-footer-progress-fill-investor"));
            fill.getStyleClass().add(fillClass);
        }
    }

    @Override
    public void onGameUpdated() {
        refresh();
    }
}
