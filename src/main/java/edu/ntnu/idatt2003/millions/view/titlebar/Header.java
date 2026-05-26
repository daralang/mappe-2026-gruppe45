package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.ingame.component.notification.NotificationPanel;
import edu.ntnu.idatt2003.millions.view.ingame.component.notification.NotificationPopupOverlay;
import edu.ntnu.idatt2003.millions.view.start.component.LanguagePicker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class Header extends HBox {

    private final Button dashboardButton;
    private final Button exchangeButton;
    private final Button leaderboardButton;
    private final Button helpButton;
    private final Button bellButton;
    private final Button newGameButton;
    private final Button saveButton;
    private final Button exitButton;
    private final Label badge;
    private final NotificationPanel notificationPanel;
    private final NotificationPopupOverlay popupOverlay;
    private final GameService gameService;

    public Header(Runnable onDashboard,
                  Runnable onExchange,
                  Runnable onSaveGame,
                  Runnable onExitGame,
                  GameService gameService) {
        this.gameService = gameService;
        getStyleClass().add("navbar");

        Label title = new Label(LanguageManager.get("app.title"));
        title.getStyleClass().add("navbar-title");

        dashboardButton = new Button(LanguageManager.get("nav.mySides"));
        exchangeButton = new Button(LanguageManager.get("nav.exchange"));
        leaderboardButton = new Button(LanguageManager.get("nav.leaderboard"));
        helpButton = new Button(LanguageManager.get("nav.help"));

        dashboardButton.getStyleClass().add("navbar-link");
        exchangeButton.getStyleClass().add("navbar-link");
        leaderboardButton.getStyleClass().add("navbar-link");
        helpButton.getStyleClass().add("navbar-link");

        dashboardButton.setOnAction(e -> onDashboard.run());
        exchangeButton.setOnAction(e -> onExchange.run());

        HBox navLinks = new HBox(60, dashboardButton, exchangeButton, leaderboardButton, helpButton);
        navLinks.setAlignment(Pos.TOP_CENTER);

        newGameButton = new Button(LanguageManager.get("nav.newGame"));
        saveButton = new Button(LanguageManager.get("nav.saveGame"));
        exitButton = new Button(LanguageManager.get("nav.exitGame"));
        newGameButton.getStyleClass().add("navbar-action");
        saveButton.getStyleClass().add("navbar-action");
        exitButton.getStyleClass().add("navbar-action");

        saveButton.setOnAction(e -> onSaveGame.run());
        exitButton.setOnAction(e -> onExitGame.run());

        HBox actions = new HBox(16, newGameButton, saveButton, exitButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        FontIcon bellIcon = new FontIcon("fth-bell");
        bellIcon.getStyleClass().add("navbar-bell-icon");
        bellButton = new Button();
        bellButton.setGraphic(bellIcon);
        bellButton.getStyleClass().add("navbar-icon");
        Tooltip.install(bellButton, new Tooltip(LanguageManager.get("notification.bell.tooltip")));

        badge = new Label();
        badge.getStyleClass().add("notification-bell-badge");
        badge.setVisible(false);
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        StackPane.setMargin(badge, new Insets(-2, 0, 0, 22));

        StackPane bellWrapper = new StackPane(bellButton, badge);
        bellWrapper.setAlignment(Pos.CENTER);
        bellWrapper.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        notificationPanel = new NotificationPanel(gameService, this::onGameUpdated);
        popupOverlay = new NotificationPopupOverlay(gameService);

        bellButton.setOnAction(e -> notificationPanel.toggle(bellButton));

        LanguagePicker languagePicker = new LanguagePicker();

        HBox bottomRight = new HBox(12, languagePicker, bellWrapper);
        bottomRight.setAlignment(Pos.CENTER_RIGHT);
        bottomRight.setPadding(new Insets(0, 0, 4, 0));

        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.TOP_RIGHT);
        rightSide.getChildren().addAll(actions, bottomRight);
        VBox.setVgrow(bottomRight, Priority.ALWAYS);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        getChildren().addAll(title, leftSpacer, navLinks, rightSpacer, rightSide);
        setAlignment(Pos.TOP_LEFT);

        LanguageManager.addObserver(this::updateTexts);
        onGameUpdated();
    }

    public void setOnLeaderboard(Runnable callback) {
        leaderboardButton.setOnAction(e -> callback.run());
    }

    public void setOnNewGame(Runnable callback) {
        newGameButton.setOnAction(e -> callback.run());
    }

    public void onGameUpdated() {
        if (gameService.getPlayer() == null) return;
        int unread = gameService.getPlayer().getUnreadNotificationCount();
        badge.setText(String.valueOf(unread));
        badge.setVisible(unread > 0);
        popupOverlay.onGameUpdated();
    }

    public Node getOverlayNode() {
        return popupOverlay.getNode();
    }

    private void updateTexts() {
        dashboardButton.setText(LanguageManager.get("nav.mySides"));
        exchangeButton.setText(LanguageManager.get("nav.exchange"));
        leaderboardButton.setText(LanguageManager.get("nav.leaderboard"));
        helpButton.setText(LanguageManager.get("nav.help"));
        newGameButton.setText(LanguageManager.get("nav.newGame"));
        saveButton.setText(LanguageManager.get("nav.saveGame"));
        exitButton.setText(LanguageManager.get("nav.exitGame"));
    }
}
