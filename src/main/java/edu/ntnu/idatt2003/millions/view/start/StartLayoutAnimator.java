package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.view.start.component.AppTabPane;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Coordinates start-screen layout binding and tab-height listeners.
 *
 * <p>Wires responsive height bindings, upload-reveal tracking, and feedback
 * height listeners for the start card. All {@link Timeline}-based animations
 * are delegated to {@link StartCardAnimations}, and construction of the
 * collapsed upload section is delegated to {@link UploadSectionFactory}.</p>
 */
public final class StartLayoutAnimator {

    private static final double START_TITLE_ESTIMATED_HEIGHT = 72;
    private static final double START_GROUP_HEIGHT_RATIO = 0.72;
    private static final double START_TAB_HEADER_HEIGHT = 60;

    private StartLayoutAnimator() {
    }

    /**
     * Wires tab pane and reserved frame heights for the start card.
     * The tab pane tracks height changes directly (upload animation)
     * and animates its height when the selected tab changes. The reserved frame is locked
     * to the new-game tab height so the title and card never shift on tab switch.
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
        double initialHeight = newGameTab.prefHeight(cardWidth + StartCardAnimations.CONTENT_WIDTH_EXTRA)
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
                    : loadGameTab.prefHeight(cardWidth + StartCardAnimations.CONTENT_WIDTH_EXTRA)
                            + START_TAB_HEADER_HEIGHT;
            StartCardAnimations.playTabHeightTransition(tabPane, target);
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

    /**
     * Wires the inline-feedback visibility of a {@link FileDropTab} to a height
     * animation that grows or shrinks the start card by the row's reserved
     * height. Used for both error and success messages, since they share the
     * same label and only differ in tone.
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
                active[0] = StartCardAnimations.playTabContentHeightTransition(tab, target);
            } else if (isLoadTabActive(tabPane, tab)) {
                active[0] = StartCardAnimations.playTabHeightTransition(tabPane, target);
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
                            + newGameTab.prefHeight(cardWidth + StartCardAnimations.CONTENT_WIDTH_EXTRA)
                            + START_TAB_HEADER_HEIGHT;
                    return Math.min(availableHeight, Math.max(proportionalHeight, contentHeight));
                },
                center.heightProperty(),
                newGameTab.minHeightProperty(),
                newGameTab.prefHeightProperty());
    }
}
