// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.view.ingame.component.notification;

import edu.ntnu.idatt2003.millions.model.notification.Notification;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.MessageFormat;
import java.util.List;

/**
 * Popup panel that displays the player's notification history.
 *
 * <p>Toggled open and closed from the notification bell in the header.
 * Marks all notifications as read and refreshes the badge count when the popup closes.</p>
 */
public class NotificationPanel {

    private static final double WIDTH = 320;
    private static final double SCROLL_MAX_HEIGHT = 420;

    private final GameService gameService;
    private final Runnable onBadgeRefresh;
    private final Popup popup;
    private final VBox listBox;
    private final Label headerLabel;
    private final Button clearButton;

    /**
     * Constructs the notification panel and wires up auto-hide and clear behaviour.
     *
     * @param gameService    the game service used to read and clear notifications
     * @param onBadgeRefresh callback invoked after notifications are cleared or the panel closes,
     *                       so the badge count in the header can be refreshed
     */
    public NotificationPanel(GameService gameService, Runnable onBadgeRefresh) {
        this.gameService = gameService;
        this.onBadgeRefresh = onBadgeRefresh;
        this.popup = new Popup();
        popup.setAutoHide(true);

        popup.setOnHidden(e -> {
            if (gameService.getPlayer() != null) {
                gameService.getPlayer().markAllNotificationsAsRead();
            }
            onBadgeRefresh.run();
        });

        headerLabel = new Label();
        headerLabel.getStyleClass().add("notification-panel-title");
        HBox.setHgrow(headerLabel, Priority.ALWAYS);

        clearButton = new Button();
        clearButton.getStyleClass().add("notification-clear-btn");
        clearButton.setOnAction(e -> {
            gameService.clearAllNotifications();
            onBadgeRefresh.run();
            refresh();
        });

        HBox header = new HBox(8, headerLabel, clearButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));

        listBox = new VBox();
        listBox.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.prefHeightProperty().bind(listBox.heightProperty().add(2));
        scroll.setMaxHeight(SCROLL_MAX_HEIGHT);
        scroll.getStyleClass().add("content-scroll");

        VBox container = new VBox(header, new Separator(), scroll);
        container.setPrefWidth(WIDTH);
        container.getStyleClass().add("notification-panel");

        popup.getContent().add(container);
    }

    /**
     * Toggles the panel open or closed relative to the given anchor node.
     *
     * @param anchor the node the popup is positioned below
     */
    public void toggle(Node anchor) {
        if (popup.isShowing()) {
            popup.hide();
        } else {
            refresh();
            Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
            popup.show(anchor.getScene().getWindow(),
                    b.getMaxX() - WIDTH,
                    b.getMaxY() + 4);
        }
    }

    /**
     * Hides the panel if it is currently showing.
     */
    public void hide() {
        popup.hide();
    }

    private void refresh() {
        listBox.getChildren().clear();

        List<Notification> all = gameService.getPlayer().getNotifications();
        List<Notification> sorted = all.reversed();

        headerLabel.setText(LanguageManager.get("notification.panel.title"));

        if (sorted.isEmpty()) {
            clearButton.setVisible(false);
            Label empty = new Label(LanguageManager.get("notification.panel.empty"));
            empty.getStyleClass().add("notification-empty");
            empty.setPadding(new Insets(24));
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            listBox.getChildren().add(empty);
        } else {
            clearButton.setText(LanguageManager.get("notification.panel.clearAll"));
            clearButton.setVisible(true);
            for (int i = 0; i < sorted.size(); i++) {
                listBox.getChildren().add(buildRow(sorted.get(i)));
                if (i < sorted.size() - 1) {
                    listBox.getChildren().add(new Separator());
                }
            }
        }
    }

    private HBox buildRow(Notification n) {
        FontIcon icon = new FontIcon(iconFor(n.severity()));
        icon.getStyleClass().addAll("notification-icon", severityIconClass(n.severity()));

        Label title = new Label(LanguageManager.get(n.titleKey()));
        title.getStyleClass().addAll("notification-title", severityTitleClass(n.severity()));
        title.setWrapText(true);

        StyledText body = StyledText.widgetLabel(formatBody(n));
        body.setWrapText(true);

        Label week = new Label(MessageFormat.format(LanguageManager.get("transactions.weekValue"), n.week()));
        week.getStyleClass().add("notification-week");

        VBox content = new VBox(2, title, body, week);
        content.setFillWidth(true);
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox row = new HBox(10, icon, content);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().addAll("notification-row", severityRowClass(n.severity()),
                n.read() ? "read" : "unread");

        return row;
    }

    /**
     * Formats the body text of a notification, resolving i18n argument placeholders.
     *
     * @param n the notification to format
     * @return the resolved and formatted body string
     */
    static String formatBody(Notification n) {
        if (n.bodyArgs().isEmpty()) {
            return LanguageManager.get(n.bodyKey());
        }
        Object[] resolved = n.bodyArgs().stream()
                .map(arg -> arg.startsWith("@") ? LanguageManager.get(arg.substring(1)) : arg)
                .toArray();
        return MessageFormat.format(LanguageManager.get(n.bodyKey()), resolved);
    }

    /**
     * Returns the Ikonli icon literal for the given notification severity.
     *
     * @param s the severity level
     * @return the icon string identifier
     */
    static String iconFor(Notification.Severity s) {
        return switch (s) {
            case SEVERE -> "fth-alert-octagon";
            case WARNING -> "fth-alert-triangle";
            case MILESTONE -> "fth-award";
            case INFO -> "fth-info";
        };
    }

    /**
     * Returns the CSS class applied to the icon for the given severity.
     *
     * @param s the severity level
     * @return the CSS class name
     */
    static String severityIconClass(Notification.Severity s) {
        return switch (s) {
            case SEVERE -> "notification-icon-severe";
            case WARNING -> "notification-icon-warning";
            case MILESTONE -> "notification-icon-milestone";
            case INFO -> "notification-icon-info";
        };
    }

    private static String severityTitleClass(Notification.Severity s) {
        return switch (s) {
            case SEVERE -> "notification-title-severe";
            case WARNING -> "notification-title-warning";
            case MILESTONE -> "notification-title-milestone";
            case INFO -> "notification-title-info";
        };
    }

    /**
     * Returns the CSS class applied to the row container for the given severity.
     *
     * @param s the severity level
     * @return the CSS class name
     */
    static String severityRowClass(Notification.Severity s) {
        return switch (s) {
            case SEVERE -> "notification-severe";
            case WARNING -> "notification-warning";
            case MILESTONE -> "notification-milestone";
            case INFO -> "notification-info";
        };
    }
}
