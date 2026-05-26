package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.view.start.component.FileDropZone;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Factory for creating the collapsed upload section on the start screen.
 *
 * <p>The returned section starts fully collapsed and transparent,
 * ready to be revealed after the first layout pass.</p>
 */
public final class UploadSectionFactory {

  private static final double UPLOAD_DROP_ZONE_HEIGHT = 190;

  private UploadSectionFactory() {
  }

  /**
   * Creates the collapsed upload section used by {@link NewGameTab}.
   *
   * @param fileDropZone the drop zone to reveal
   * @param currencyRow  the currency selector row shown below the drop zone
   * @param maxWidth     the maximum visual width for the upload section
   * @return a collapsed upload {@link VBox}
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
}
