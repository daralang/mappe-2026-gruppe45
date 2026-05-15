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
import javafx.scene.layout.Region;
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
    private static final double UPLOAD_SECTION_MIN_HEIGHT = 200;
    private static final double UPLOAD_SECTION_BUFFER = 8;
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
            tabContent.layout();
            double collapsedHeight = tabContent.prefHeight(cardWidth + CONTENT_WIDTH_EXTRA);
            double uploadExpandedHeight = computeUploadSectionExpandedHeight(uploadSection, cardWidth);
            double expandedHeight = collapsedHeight + uploadExpandedHeight;

            tabContent.setMinHeight(collapsedHeight);
            tabContent.setPrefHeight(collapsedHeight);
            tabContent.setMaxHeight(expandedHeight);

            PauseTransition delay = new PauseTransition(UPLOAD_REVEAL_DELAY);
            delay.setOnFinished(event -> playUploadTimeline(
                    tabContent, uploadSection, collapsedHeight, expandedHeight, uploadExpandedHeight));
            delay.play();
        });
    }

    /**
     * Computes the natural expanded height of {@code uploadSection} by summing
     * the preferred heights of its {@link Region} children and the spacing
     * between them, plus a small safety buffer to absorb font and rendering
     * rounding so the section's clip never cuts visible content.
     *
     * @param uploadSection the upload section whose children determine the height
     * @param width         the width used to compute each child's preferred height
     * @return the target expanded height in pixels
     */
    private static double computeUploadSectionExpandedHeight(VBox uploadSection, double width) {
        double sum = 0;
        int count = 0;
        for (Node child : uploadSection.getChildrenUnmodifiable()) {
            if (child instanceof Region region) {
                sum += region.prefHeight(width);
                count++;
            }
        }
        if (count > 1) {
            sum += uploadSection.getSpacing() * (count - 1);
        }
        return Math.max(UPLOAD_SECTION_MIN_HEIGHT, sum + UPLOAD_SECTION_BUFFER);
    }

    /**
     * Wires tab pane and reserved frame heights for the start card.
     *
     * <p>The tab pane tracks {@link NewGameTab} height changes directly (upload animation)
     * and animates its height when the selected tab changes. The reserved frame is locked
     * to the new-game tab height so the title and card never shift on tab switch.</p>
     *
     *  Inline feedback labels in both tabs are animated in/out by listening on
     *  {@link FileDropTab#feedbackVisibleProperty()} and driving the surrounding
     *  height by the exact pixel amount the feedback row reserves, obtained
     *  from {@link FileDropTab#computeFeedbackReservedHeight()}. Listening on
     *  the visibility property avoids the chicken-and-egg situation where the
     *  button area cannot grow because the tab pane is locked, and therefore
     *  no layout listener ever fires.
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

        // Drive tab-content height explicitly from each tab's feedbackVisibleProperty
        // instead of waiting for the parent VBox to grow on its own — the parent
        // is constrained while the tab pane is locked, so the layout listener
        // would never observe a delta.
        bindFeedbackHeightAnimation(newGameTab, tabPane, true);
        bindFeedbackHeightAnimation(loadGameTab, tabPane, false);

        reservedStartGroup.prefHeightProperty().bind(createResponsiveStartGroupHeight(
                center, newGameTab, cardWidth, groupSpacing));
        reservedStartGroup.minHeightProperty().bind(reservedStartGroup.prefHeightProperty());
        reservedStartGroup.maxHeightProperty().bind(reservedStartGroup.prefHeightProperty());
    }

    private static void playUploadTimeline(VBox tabContent,
                                           VBox uploadSection,
                                           double collapsedHeight,
                                           double expandedHeight,
                                           double uploadExpandedHeight) {
        Timeline reveal = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(uploadSection.prefHeightProperty(), 0),
                        new KeyValue(uploadSection.maxHeightProperty(), 0),
                        new KeyValue(uploadSection.opacityProperty(), 0),
                        new KeyValue(tabContent.minHeightProperty(), collapsedHeight),
                        new KeyValue(tabContent.prefHeightProperty(), collapsedHeight)),
                new KeyFrame(UPLOAD_REVEAL_DURATION,
                        new KeyValue(uploadSection.prefHeightProperty(),
                                uploadExpandedHeight, Interpolator.EASE_BOTH),
                        new KeyValue(uploadSection.maxHeightProperty(),
                                uploadExpandedHeight, Interpolator.EASE_BOTH),
                        new KeyValue(uploadSection.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(tabContent.minHeightProperty(), expandedHeight, Interpolator.EASE_BOTH),
                        new KeyValue(tabContent.prefHeightProperty(), expandedHeight, Interpolator.EASE_BOTH))
        );
        reveal.setOnFinished(finished -> {
            uploadSection.setPrefHeight(uploadExpandedHeight);
            uploadSection.setMaxHeight(uploadExpandedHeight);
            uploadSection.setOpacity(1);
            tabContent.setMinHeight(expandedHeight);
            tabContent.setPrefHeight(expandedHeight);
            // Leave maxHeight unconstrained so the feedback label can push height upward.
            tabContent.setMaxHeight(Double.MAX_VALUE);
        });
        reveal.play();
    }

    /**
     * Animates the tab pane height from its current value to {@code targetHeight}.
     *
     * @param tabPane      the tab pane to animate
     * @param targetHeight the target height in pixels
     * @return the running {@link Timeline}, so callers can stop it if a follow-up
     *         animation needs to override it before completion
     */
    private static Timeline playTabHeightTransition(AppTabPane tabPane, double targetHeight) {
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
        return timeline;
    }

    /**
     * Wires the inline-feedback visibility of a {@link FileDropTab} to a height
     * animation that grows or shrinks the start card by the row's reserved
     * height. Used for both error and success messages, since they share the
     * same label and only differ in tone.
     *
     * @param tab       the file drop tab whose feedback visibility drives the animation
     * @param tabPane   the surrounding tab pane
     * @param isNewGame {@code true} when {@code tab} is the new-game tab; otherwise it is
     *                  the load-game tab and only animates when actively selected
     */
    private static void bindFeedbackHeightAnimation(FileDropTab tab,
                                                    AppTabPane tabPane,
                                                    boolean isNewGame) {
        double[] baseHeight = {Double.NaN};
        Timeline[] active = {null};
        tab.feedbackVisibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (Double.isNaN(baseHeight[0])) {
                baseHeight[0] = isNewGame ? tab.getPrefHeight() : tabPane.getPrefHeight();
            }
            if (active[0] != null) {
                active[0].stop();
                active[0] = null;
            }
            double target = isVisible
                    ? baseHeight[0] + tab.computeFeedbackReservedHeight()
                    : baseHeight[0];
            if (isNewGame) {
                active[0] = playTabContentHeightTransition(tab, target);
            } else if (isLoadTabActive(tabPane, tab)) {
                active[0] = playTabHeightTransition(tabPane, target);
            }
        });
    }

    /**
     * Returns whether the given load tab is the currently selected content
     * inside the tab pane.
     *
     * @param tabPane the surrounding tab pane
     * @param loadTab the load-game tab content
     * @return {@code true} when the load tab is the active selection
     */
    private static boolean isLoadTabActive(AppTabPane tabPane, FileDropTab loadTab) {
        Node selected = tabPane.getSelectionModel().getSelectedItem() == null
                ? null : tabPane.getSelectionModel().getSelectedItem().getContent();
        return selected == loadTab;
    }

    /**
     * Animates the {@code min}, {@code pref} and {@code max} height of a tab
     * content {@link FileDropTab} from its current preferred height to
     * {@code targetHeight}. Sister method of {@link #playTabHeightTransition}
     * but operates on the content {@code VBox} instead of the tab pane.
     *
     * @param content      the tab content to animate
     * @param targetHeight the target height in pixels
     * @return the running {@link Timeline}, so callers can stop it if a follow-up
     *         animation needs to override it before completion
     */
    private static Timeline playTabContentHeightTransition(FileDropTab content, double targetHeight) {
        double fromHeight = content.getPrefHeight();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(content.prefHeightProperty(), fromHeight),
                        new KeyValue(content.minHeightProperty(), fromHeight),
                        new KeyValue(content.maxHeightProperty(), fromHeight)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(content.prefHeightProperty(), targetHeight, Interpolator.EASE_BOTH),
                        new KeyValue(content.minHeightProperty(), targetHeight, Interpolator.EASE_BOTH),
                        new KeyValue(content.maxHeightProperty(), targetHeight, Interpolator.EASE_BOTH))
        );
        timeline.play();
        return timeline;
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
