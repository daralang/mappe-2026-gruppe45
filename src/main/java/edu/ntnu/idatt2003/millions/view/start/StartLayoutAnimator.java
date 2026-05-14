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
import javafx.scene.control.Tab;
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
    private static final double START_TAB_HEADER_HEIGHT = 104;
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
     * Binds the start card and reserved frame heights used by {@link StartView}.
     *
     * <p>The tab pane follows the selected tab content height. The reserved
     * frame is centered in the available area, but top-anchors its child so the
     * upload reveal grows downward instead of re-centering during animation.</p>
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
        DoubleBinding selectedTabHeight = createSelectedTabHeightBinding(tabPane,
                newGameTab, loadGameTab, cardWidth);
        tabPane.minHeightProperty().bind(selectedTabHeight);
        tabPane.prefHeightProperty().bind(selectedTabHeight);
        tabPane.maxHeightProperty().bind(selectedTabHeight);

        reservedStartGroup.prefHeightProperty().bind(createResponsiveStartGroupHeight(
                center, tabPane, newGameTab, loadGameTab, cardWidth, groupSpacing));
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

    private static DoubleBinding createSelectedTabHeightBinding(AppTabPane tabPane,
                                                                NewGameTab newGameTab,
                                                                LoadGameTab loadGameTab,
                                                                double cardWidth) {
        return Bindings.createDoubleBinding(
                () -> getSelectedTabHeight(tabPane, newGameTab, cardWidth),
                tabPane.getSelectionModel().selectedItemProperty(),
                tabPane.widthProperty(),
                newGameTab.minHeightProperty(),
                newGameTab.prefHeightProperty(),
                loadGameTab.minHeightProperty(),
                loadGameTab.prefHeightProperty());
    }

    private static DoubleBinding createResponsiveStartGroupHeight(StackPane center,
                                                                  AppTabPane tabPane,
                                                                  NewGameTab newGameTab,
                                                                  LoadGameTab loadGameTab,
                                                                  double cardWidth,
                                                                  double groupSpacing) {
        return Bindings.createDoubleBinding(
                () -> {
                    double availableHeight = center.getHeight();
                    double proportionalHeight = availableHeight * START_GROUP_HEIGHT_RATIO;
                    double contentHeight = START_TITLE_ESTIMATED_HEIGHT
                            + groupSpacing
                            + getSelectedTabHeight(tabPane, newGameTab, cardWidth);
                    return Math.min(availableHeight, Math.max(proportionalHeight, contentHeight));
                },
                center.heightProperty(),
                tabPane.getSelectionModel().selectedItemProperty(),
                tabPane.widthProperty(),
                newGameTab.minHeightProperty(),
                newGameTab.prefHeightProperty(),
                loadGameTab.minHeightProperty(),
                loadGameTab.prefHeightProperty());
    }

    private static double getSelectedTabHeight(AppTabPane tabPane, Node fallbackContent, double cardWidth) {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        Node content = selectedTab == null ? fallbackContent : selectedTab.getContent();
        double contentWidth = tabPane.getWidth() > 0 ? tabPane.getWidth() : cardWidth;
        return content.prefHeight(contentWidth) + START_TAB_HEADER_HEIGHT;
    }
}
