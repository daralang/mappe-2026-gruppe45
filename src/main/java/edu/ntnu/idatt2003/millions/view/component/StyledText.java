package edu.ntnu.idatt2003.millions.view.component;

import javafx.scene.control.Label;

/**
 * A label that applies a predefined CSS typography class.
 *
 * <p>Use the static factory methods to create labels with
 * the correct style for their context.
 *
 * <p>Example usage:
 * <pre>
 *   Label nameLabel  = StyledText.paragraph();
 *   Label titleLabel = StyledText.heading3("Navn");
 * </pre>
 */
public class StyledText extends Label {

    private StyledText(String cssClass) {
        getStyleClass().add(cssClass);
    }

    private StyledText(String text, String cssClass) {
        super(text);
        getStyleClass().add(cssClass);
    }

    /** @return an empty label styled as heading-1 */
    public static StyledText HEADING_ONE() { return new StyledText("heading-1"); }

    /** @return a label styled as heading-1 */
    public static StyledText HEADING_ONE(String text) { return new StyledText(text, "heading-1"); }

    /** @return an empty label styled as heading-2 */
    public static StyledText HEADING_TWO() { return new StyledText("heading-2"); }

    /** @return a label styled as heading-2 */
    public static StyledText HEADING_TWO(String text) { return new StyledText(text, "heading-2"); }

    /** @return an empty label styled as heading-3 */
    public static StyledText HEADING_THREE() { return new StyledText("heading-3"); }

    /** @return a label styled as heading-3 */
    public static StyledText HEADING_THREE(String text) { return new StyledText(text, "heading-3"); }

    /** @return an empty label styled as paragraph-1 */
    public static StyledText PARAGRAPH_ONE() { return new StyledText("paragraph-1"); }

    /** @return a label styled as paragraph-1 */
    public static StyledText PARAGRAPH_ONE(String text) { return new StyledText(text, "paragraph-1"); }
}