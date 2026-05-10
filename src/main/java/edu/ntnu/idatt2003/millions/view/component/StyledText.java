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
 *   Label nameLabel  = StyledText.paragraphOne();
 *   Label titleLabel = StyledText.headingOne("Navn");
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
    public static StyledText headingOne() { return new StyledText("heading-1"); }

    /** @return a label styled as heading-1 */
    public static StyledText headingOne(String text) { return new StyledText(text, "heading-1"); }

    /** @return an empty label styled as paragraph-1 */
    public static StyledText paragraphOne() { return new StyledText("paragraph-1"); }

    /** @return a label styled as paragraph-1 */
    public static StyledText paragraphOne(String text) { return new StyledText(text, "paragraph-1"); }

    /** @return an empty label styled as page-title */
    public static StyledText pageTitle() { return new StyledText("page-title"); }

    /** @return a label styled as page-title */
    public static StyledText pageTitle(String text) { return new StyledText(text, "page-title"); }

    /** @return an empty label styled as section-title */
    public static StyledText sectionTitle() { return new StyledText("section-title"); }

    /** @return a label styled as section-title */
    public static StyledText sectionTitle(String text) { return new StyledText(text, "section-title"); }

    /** @return an empty label styled as week-label */
    public static StyledText weekLabel() { return new StyledText("week-label"); }

    /** @return a label styled as week-label */
    public static StyledText weekLabel(String text) { return new StyledText(text, "week-label"); }

    /** @return an empty label styled as widget-label */
    public static StyledText widgetLabel() { return new StyledText("widget-label"); }

    /** @return a label styled as widget-label */
    public static StyledText widgetLabel(String text) { return new StyledText(text, "widget-label"); }

    /** @return an empty label styled as widget-value */
    public static StyledText widgetValue() { return new StyledText("widget-value"); }

    /** @return a label styled as widget-value */
    public static StyledText widgetValue(String text) { return new StyledText(text, "widget-value"); }

    /** @return an empty label styled as widget-change */
    public static StyledText widgetChange() { return new StyledText("widget-change"); }

    /** @return a label styled as widget-change */
    public static StyledText widgetChange(String text) { return new StyledText(text, "widget-change"); }

    /** @return an empty label styled as detail-label */
    public static StyledText detailLabel() { return new StyledText("detail-label"); }

    /** @return a label styled as detail-label */
    public static StyledText detailLabel(String text) { return new StyledText(text, "detail-label"); }

    /** @return an empty label styled as detail-value */
    public static StyledText detailValue() { return new StyledText("detail-value"); }

    /** @return a label styled as detail-value */
    public static StyledText detailValue(String text) { return new StyledText(text, "detail-value"); }
}
