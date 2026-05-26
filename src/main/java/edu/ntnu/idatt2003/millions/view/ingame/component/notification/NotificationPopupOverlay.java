package edu.ntnu.idatt2003.millions.view.ingame.component.notification;

import edu.ntnu.idatt2003.millions.model.notification.Notification;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class NotificationPopupOverlay {

    private static final int MAX_VISIBLE = 3;
    private static final double TOAST_WIDTH = 280;
    private static final double AUTO_DISMISS_SECONDS = 5;

    private final GameService gameService;
    private final VBox stack;
    private final List<ToastItem> visible = new ArrayList<>();
    private final Deque<Notification> queue = new ArrayDeque<>();
    private int lastShownId = 0;

    public NotificationPopupOverlay(GameService gameService) {
        this.gameService = gameService;
        this.stack = new VBox(8);
        stack.setFillWidth(true);
        stack.setPrefWidth(TOAST_WIDTH);
        stack.setMaxWidth(TOAST_WIDTH);
        stack.setPickOnBounds(false);
        stack.setVisible(false);
        this.lastShownId = gameService.getPlayer().getNotifications().stream()
                .mapToInt(Notification::id)
                .max()
                .orElse(0);
    }

    public Node getNode() {
        return stack;
    }

    public void onGameUpdated() {
        List<Notification> all = gameService.getPlayer().getNotifications();
        List<Notification> fresh = all.stream()
                .filter(n -> n.id() > lastShownId)
                .toList();

        if (fresh.isEmpty()) return;

        lastShownId = fresh.stream().mapToInt(Notification::id).max().orElse(lastShownId);

        for (Notification n : fresh) {
            if (visible.size() < MAX_VISIBLE) {
                showToast(n);
            } else {
                queue.addLast(n);
            }
        }
    }

    private void showToast(Notification n) {
        Region toast = buildToast(n);
        stack.getChildren().add(0, toast);
        stack.setVisible(true);

        toast.setTranslateX(TOAST_WIDTH);
        TranslateTransition slide = new TranslateTransition(Duration.millis(250), toast);
        slide.setToX(0);
        slide.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(AUTO_DISMISS_SECONDS));

        ToastItem item = new ToastItem(toast, pause);
        visible.add(item);

        pause.setOnFinished(e -> dismissToast(item));

        toast.setOnMouseEntered(e -> pause.pause());
        toast.setOnMouseExited(e -> pause.play());

        pause.play();
    }

    private Region buildToast(Notification n) {
        FontIcon icon = new FontIcon(NotificationPanel.iconFor(n.severity()));
        icon.getStyleClass().addAll("notification-icon",
                NotificationPanel.severityIconClass(n.severity()));

        Label title = new Label(LanguageManager.get(n.titleKey()));
        title.getStyleClass().addAll("notification-toast-title",
                NotificationPanel.severityRowClass(n.severity()) + "-title");
        title.setWrapText(true);
        HBox.setHgrow(title, Priority.ALWAYS);

        HBox titleRow = new HBox(6, icon, title);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label body = new Label(NotificationPanel.formatBody(n));
        body.getStyleClass().add("notification-toast-body");
        body.setWrapText(true);

        VBox content = new VBox(4, titleRow, body);
        content.setPadding(new Insets(12, 32, 12, 16));

        FontIcon closeBtnIcon = new FontIcon("fth-x");
        closeBtnIcon.getStyleClass().add("notification-toast-close-icon");
        Button closeBtn = new Button();
        closeBtn.setGraphic(closeBtnIcon);
        closeBtn.getStyleClass().add("notification-toast-close");

        StackPane toast = new StackPane(content, closeBtn);
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new Insets(8, 8, 0, 0));
        toast.setPrefWidth(TOAST_WIDTH);
        toast.getStyleClass().addAll("notification-toast",
                NotificationPanel.severityRowClass(n.severity()));

        closeBtn.setOnAction(e -> {
            ToastItem found = visible.stream()
                    .filter(ti -> ti.node() == toast)
                    .findFirst()
                    .orElse(null);
            if (found != null) dismissToast(found);
        });

        return toast;
    }

    private void dismissToast(ToastItem item) {
        if (!visible.remove(item)) return;
        item.timer().stop();

        FadeTransition fade = new FadeTransition(Duration.millis(300), item.node());
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            stack.getChildren().remove(item.node());
            if (stack.getChildren().isEmpty()) {
                stack.setVisible(false);
            }
            if (!queue.isEmpty() && visible.size() < MAX_VISIBLE) {
                showToast(queue.pollFirst());
            }
        });
        fade.play();
    }

    private record ToastItem(Region node, PauseTransition timer) {}
}
