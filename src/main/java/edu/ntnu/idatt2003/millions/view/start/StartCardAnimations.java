package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.view.start.component.AppTabPane;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Pure animation utilities for the start-screen card.
 */
public final class StartCardAnimations {

  /**
   * Extra pixels added to {@code cardWidth} when measuring preferred heights,
   * shared with {@link StartLayoutAnimator} to avoid duplicating the value.
   */
  static final double CONTENT_WIDTH_EXTRA = 48;

  private static final double UPLOAD_SECTION_MIN_HEIGHT = 200;
  private static final double UPLOAD_SECTION_BUFFER = 8;
  private static final Duration UPLOAD_REVEAL_DELAY = Duration.millis(380);
  private static final Duration UPLOAD_REVEAL_DURATION = Duration.millis(820);

  private StartCardAnimations() {
  }

  /**
   * Plays the automatic upload reveal for {@link NewGameTab}.
   *
   * <p>Defers until after the first layout pass,
   * measures the collapsed tab height, then expands the upload section downward
   * while the tab content animates its preferred height so the surrounding
   * {@link AppTabPane} can resize the start card during the reveal.</p>
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
   * Animates the {@link AppTabPane} height from its current value to
   * {@code targetHeight}.
   *
   * @param tabPane      the tab pane to animate
   * @param targetHeight the target height in pixels
   * @return the running {@link Timeline}, so callers can stop it if a follow-up
   *         animation needs to override it before completion
   */
  static Timeline playTabHeightTransition(AppTabPane tabPane, double targetHeight) {
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
   * Animates the {@code min}, {@code pref} and {@code max} height of a
   * {@link FileDropTab} content node from its current preferred height to
   * {@code targetHeight}. Sister method of {@link #playTabHeightTransition}
   * but operates on the content {@link VBox} instead of the tab pane.
   *
   * @param content      the tab content to animate
   * @param targetHeight the target height in pixels
   * @return the running {@link Timeline}, so callers can stop it if a follow-up
   *         animation needs to override it before completion
   */
  static Timeline playTabContentHeightTransition(FileDropTab content, double targetHeight) {
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
   * Computes the natural expanded height of {@code uploadSection} by summing
   * the preferred heights of its children and the spacing
   * between them, plus a small safety buffer to absorb font and rendering
   * rounding so the clip never cuts visible content.
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
   * Builds and plays the upload reveal {@link Timeline} after the initial delay.
   * Animates upload section height and opacity together with the owning tab
   * content height, then locks all values to their final state on completion.
   *
   * @param tabContent          the tab content node being expanded
   * @param uploadSection       the upload section being revealed
   * @param collapsedHeight     the tab content height before the reveal
   * @param expandedHeight      the tab content height after the reveal
   * @param uploadExpandedHeight the natural expanded height of the upload section
   */
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
}
