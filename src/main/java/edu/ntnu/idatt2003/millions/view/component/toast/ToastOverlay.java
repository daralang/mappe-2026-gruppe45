package edu.ntnu.idatt2003.millions.view.component.toast;

import edu.ntnu.idatt2003.millions.service.toast.ToastService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Overlay that renders a single toast at a time in the bottom-right corner.
 *
 * <p>Add this as a child of the application's root {@link StackPane}.
 * Positioning constraints are set in the constructor so the caller
 * only needs {@code outerRoot.getChildren().add(toastOverlay)}.</p>
 */
public class ToastOverlay extends StackPane {

    private static final Duration AUTO_DISMISS = Duration.seconds(3);
    private static final Duration FADE_DURATION = Duration.millis(200);

    private ToastNode current;
    private PauseTransition dismissTimer;

    public ToastOverlay(ToastService toastService) {
        setPickOnBounds(false);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(this, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(this, new Insets(0, 16, 0, 0));
        toastService.addListener(toast -> Platform.runLater(() -> display(toast)));
    }

    private void display(Toast toast) {
        if (current != null) {
            stopDismissTimer();
            animateOutAndReplace(toast);
        } else {
            showNew(toast);
        }
    }

    private void showNew(Toast toast) {
        current = new ToastNode(toast);
        getChildren().add(current);
        animateIn(current);
        scheduleDismiss();
    }

    private void animateIn(ToastNode node) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(FADE_DURATION, node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    private void animateOutAndReplace(Toast newToast) {
        ToastNode outgoing = current;
        FadeTransition fade = new FadeTransition(FADE_DURATION, outgoing);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            getChildren().remove(outgoing);
            showNew(newToast);
        });
        fade.play();
    }

    private void scheduleDismiss() {
        dismissTimer = new PauseTransition(AUTO_DISMISS);
        dismissTimer.setOnFinished(e -> dismissCurrent());
        dismissTimer.play();

        current.setOnMouseEntered(e -> dismissTimer.pause());
        current.setOnMouseExited(e -> dismissTimer.play());
    }

    private void dismissCurrent() {
        if (current == null) return;
        ToastNode toRemove = current;
        current = null;
        FadeTransition fade = new FadeTransition(FADE_DURATION, toRemove);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> getChildren().remove(toRemove));
        fade.play();
    }

    private void stopDismissTimer() {
        if (dismissTimer != null) {
            dismissTimer.stop();
        }
    }
}
