package edu.ntnu.idatt2003.millions.view.component;

import javafx.scene.control.Label;

/**
 * A small discreet info icon ("ⓘ") that signals interactivity visually.
 * Tooltip behaviour is owned by the enclosing HBox — attach via
 * {@link Tooltips#attach(javafx.scene.Node, String)} on the parent container
 * so the tooltip triggers anywhere over the label-and-icon row.
 * Intended to sit inline with labels using a small HBox with 4–6 px spacing.
 */
public class InfoIcon extends Label {

    public InfoIcon() {
        super("ⓘ");
        getStyleClass().add("tooltip-info-icon");
    }
}
