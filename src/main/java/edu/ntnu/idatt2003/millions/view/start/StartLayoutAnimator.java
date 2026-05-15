package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.view.StartView;
import edu.ntnu.idatt2003.millions.view.component.AppTabPane;
import edu.ntnu.idatt2003.millions.view.component.FileDropZone;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Coordinates start-screen layout sizing and reveal animations.
 *
 * <p>This component keeps JavaFX animation and responsive height binding logic
 * out of {@link StartView} and {@link NewGameTab}. It owns only view concerns:
 * collapsed upload-section setup, the automatic downward upload reveal, and the
 * responsive card frame that lets the active tab resize cleanly.</p>
 */
public final class StartLayoutAnimator {

    private static final double START_TITLE_ESTIMATED_HEIGHT = 72;
    private static final double START_GROUP_HEIGHT_RATIO = 0.72;
    private static final double START_TAB_HEADER_HEIGHT = 60;
    private static final double UPLOAD_DROP_ZONE_HEIGHT = 190;
    private static final double UPLOAD_SECTION_EXPANDED_HEIGHT = 240;
    private static final double CONTENT_WIDTH_EXTRA = 48;
    private static final Duration UPLOAD_REVEAL_DELAY = Duration.millis(380);
    private static final Duration UPLOAD_REVEAL_DURATION = Duration.millis(820);

    private StartLayoutAnimator() {
    }

    /**
     * Creates the collapsed upload section used by {@link NewGameTab}.
     *
     * <p>The returned section is clipped to its animated height, starts fully
     * collapsed and transparent, and contains the provided {@link FileDropZone}
     * followed by the currency row.</p>
     *
     * @param fileDropZone the drop zone to reveal
     * @param currencyRow  the currency selector row shown below the drop zone
     * @param maxWidth     the maximum visual width for the upload section
     * @return a collapsed upload {@link VBox} ready for {@link #playUploadIntro(VBox, VBox, double)}
     */
    public static VBox createCollapsedUploadSection(FileDropZone fileDropZone,
                                                    Node currencyRow,
                                                    double maxWidth) {
        fileDropZone.setMinHeight(UPLOAD_DROP_ZONE_HEIGHT);
        fileDropZone.setPrefHeight(UPLOAD_DROP_ZONE_HEIGHT);

        VBox uploadSection = new VBox(16, fileDropZone, currencyRow);
        uploadSection.setMaxWidth(maxWidth);
        uploadSection.setMinHeight(0);
        uploadSection.setPrefHeight(0);
        uploadSection.setMaxHeight(0);
        uploadSection.setOpacity(0);

        Rectangle uploadClip = new Rectangle();
        uploadClip.widthProperty().bind(uploadSection.widthProperty());
        uploadClip.heightProperty().bind(uploadSection.heightProperty());
        uploadSection.setClip(uploadClip);

        return uploadSection;
    }

    /**
     * Plays the automatic upload reveal for {@link NewGameTab}.
     *
     * <p>The upload section expands downward while the owning tab content
     * animates its own preferred height, allowing the surrounding {@link AppTabPane}
     * to resize the white start card during the reveal.</p>
     *
     * @param tabContent    the tab content whose height should follow the reveal
     * @param uploadSection the collapsed upload section to reveal
     * @param cardWidth     the visual card content width
     */
    public static void playUploadIntro(VBox tabContent, VBox uploadSection, double cardWidth) {
        Platform.runLater(() -> {
            tabContent.applyCss();
            double collapsedHeight = tabContent.prefHeight(cardWidth + CONTENT_WIDTH_EXTRA);
            double expandedHeight = collapsedHeight + UPLOAD_SECTION_EXPANDED_HEIGHT;

            tabContent.setMinHeight(collapsedHeight);
            tabContent.setPrefHeight(collapsedHeight);
            tabContent.setMaxHeight(expandedHeight);

            PauseTransition delay = new PauseTransition(UPLOAD_REVEAL_DELAY);
            delay.setOnFinished(event -> playUploadTimeline(tabContent, uploadSection, collapsedHeight, expandedHeight));
            delay.play();
        });
    }

    /**
     * Wires tab pane and reserved frame heights for the start card.
     *
     * <p>The tab pane tracks {@link NewGameTab} height changes directly (upload animation)
     * and animates its height when the selected tab changes. The reserved frame is locked
     * to the new-game tab height so the title and card never shift on tab switch.</p>
     *
     * @param tabPane            the start card tab pane
     * @param center             the available center area
     * @param reservedStartGroup the centered frame around title and card
     * @param newGameTab         the new-game tab content
     * @param loadGameTab        the load-game tab content
     * @param cardWidth          the preferred card width
     * @param groupSpacing       the spacing between title and card
     */
    public static void bindStartCardLayout(AppTabPane tabPane,
                                           StackPane center,
                                           StackPane reservedStartGroup,
                                           NewGameTab newGameTab,
                                           LoadGameTab loadGameTab,
                                           double cardWidth,
                                           double groupSpacing) {
        // Set initial tab pane height before upload animation starts
        double initialHeight = newGameTab.prefHeight(cardWidth + CONTENT_WIDTH_EXTRA)
                + START_TAB_HEADER_HEIGHT;
        tabPane.setMinHeight(initialHeight);
        tabPane.setPrefHeight(initialHeight);
        tabPane.setMaxHeight(initialHeight);

        // Follow newGameTab height changes during the upload reveal animation
        newGameTab.prefHeightProperty().addListener((obs, old, h) -> {
            Node selected = tabPane.getSelectionModel().getSelectedItem() == null
                    ? null : tabPane.getSelectionModel().getSelectedItem().getContent();
            if (selected == newGameTab) {
                double height = h.doubleValue() + START_TAB_HEADER_HEIGHT;
                tabPane.setMinHeight(height);
                tabPane.setPrefHeight(height);
                tabPane.setMaxHeight(height);
            }
        });

        // Animate tab pane height on tab switch
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == null) return;
            double target = newTab.getContent() == newGameTab
                    ? newGameTab.getPrefHeight() + START_TAB_HEADER_HEIGHT
                    : loadGameTab.prefHeight(cardWidth + CONTENT_WIDTH_EXTRA) + START_TAB_HEADER_HEIGHT;
            playTabHeightTransition(tabPane, target);
        });

        reservedStartGroup.prefHeightProperty().bind(createResponsiveStartGroupHeight(
                center, newGameTab, cardWidth, groupSpacing));
        reservedStartGroup.minHeightProperty().bind(reservedStartGroup.prefHeightProperty());
        reservedStartGroup.maxHeightProperty().bind(reservedStartGroup.prefHeightProperty());
    }

    private static void playUploadTimeline(VBox tabContent,
                                           VBox uploadSection,
                                           double collapsedHeight,
                                           double expandedHeight) {
        Timeline reveal = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(uploadSection.prefHeightProperty(), 0),
                        new KeyValue(uploadSection.maxHeightProperty(), 0),
                        new KeyValue(uploadSection.opacityProperty(), 0),
                        new KeyValue(tabContent.minHeightProperty(), collapsedHeight),
                        new KeyValue(tabContent.prefHeightProperty(), collapsedHeight)),
                new KeyFrame(UPLOAD_REVEAL_DURATION,
                        new KeyValue(uploadSection.prefHeightProperty(),
                                UPLOAD_SECTION_EXPANDED_HEIGHT, Interpolator.EASE_BOTH),
                        new KeyValue(uploadSection.maxHeightProperty(),
                                UPLOAD_SECTION_EXPANDED_HEIGHT, Interpolator.EASE_BOTH),
                        new KeyValue(uploadSection.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(tabContent.minHeightProperty(), expandedHeight, Interpolator.EASE_BOTH),
                        new KeyValue(tabContent.prefHeightProperty(), expandedHeight, Interpolator.EASE_BOTH))
        );
        reveal.setOnFinished(finished -> {
            uploadSection.setPrefHeight(UPLOAD_SECTION_EXPANDED_HEIGHT);
            uploadSection.setMaxHeight(UPLOAD_SECTION_EXPANDED_HEIGHT);
            uploadSection.setOpacity(1);
            tabContent.setMinHeight(expandedHeight);
            tabContent.setPrefHeight(expandedHeight);
            tabContent.setMaxHeight(expandedHeight);
        });
        reveal.play();
    }

    /**
     * Animates the tab pane height from its current value to {@code targetHeight}.
     *
     * @param tabPane      the tab pane to animate
     * @param targetHeight the target height in pixels
     */
    private static void playTabHeightTransition(AppTabPane tabPane, double targetHeight) {
        double fromHeight = tabPane.getPrefHeight();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(tabPane.prefHeightProperty(), fromHeight),
                        new KeyValue(tabPane.minHeightProperty(), fromHeight),
                        new KeyValue(tabPane.maxHeightProperty(), fromHeight)),
                new KeyFrame(Duration.millis(350),
                        new KeyValue(tabPane.prefHeightProperty(), targetHeight, Interpolator.EASE_BOTH),
                        new KeyValue(tabPane.minHeightProperty(), targetHeight, Interpolator.EASE_BOTH),
                        new KeyValue(tabPane.maxHeightProperty(), targetHeight, Interpolator.EASE_BOTH))
        );
        timeline.play();
    }

    /**
     * Returns a binding that locks the start group height to the new-game tab,
     * preventing layout shifts when the selected tab changes.
     *
     * @param center       the available center area
     * @param newGameTab   the new-game tab content
     * @param cardWidth    the preferred card content width
     * @param groupSpacing the spacing between title and card
     * @return a {@link DoubleBinding} for the reserved start group height
     */
    private static DoubleBinding createResponsiveStartGroupHeight(StackPane center,
                                                                  NewGameTab newGameTab,
                                                                  double cardWidth,
                                                                  double groupSpacing) {
        return Bindings.createDoubleBinding(
                () -> {
                    double availableHeight = center.getHeight();
                    double proportionalHeight = availableHeight * START_GROUP_HEIGHT_RATIO;
                    // Always use newGameTab height to keep title and card position stable
                    double contentHeight = START_TITLE_ESTIMATED_HEIGHT
                            + groupSpacing
                            + newGameTab.prefHeight(cardWidth + CONTENT_WIDTH_EXTRA)
                            + START_TAB_HEADER_HEIGHT;
                    return Math.min(availableHeight, Math.max(proportionalHeight, contentHeight));
                },
                center.heightProperty(),
                newGameTab.minHeightProperty(),
                newGameTab.prefHeightProperty());
    }
}
