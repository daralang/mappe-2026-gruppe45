package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Reusable search bar for views that need explicit search input.
 * Assembles a search icon, a text field, a search button, and a
 * clear button into a single row. Search is triggered by pressing Enter or
 * clicking the search button. The clear button is hidden until a non-empty
 * search has been submitted; clicking it resets the field and notifies the
 * caller.
 *
 */
public class SearchBar extends VBox {

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button();
    private final Button clearButton = new Button("✕");
    private final String placeholderKey;
    private final String buttonKey;

    /**
     * Creates a search bar with localized text.
     *
     * @param placeholderKey the i18n key for the field placeholder
     * @param buttonKey      the i18n key for the button text
     * @param onSearch       callback receiving the current search term on each
     *                       triggered search or clear action
     * @throws NullPointerException if any argument is null
     */
    public SearchBar(String placeholderKey, String buttonKey, Consumer<String> onSearch) {
        Objects.requireNonNull(placeholderKey, "Placeholder key cannot be null");
        Objects.requireNonNull(buttonKey, "Button key cannot be null");
        Objects.requireNonNull(onSearch, "Search callback cannot be null");
        this.placeholderKey = placeholderKey;
        this.buttonKey = buttonKey;

        Label iconLabel = new Label("🔍");
        iconLabel.getStyleClass().add("search-icon");

        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchButton.getStyleClass().add("search-button");

        clearButton.getStyleClass().add("search-clear-button");
        clearButton.setOpacity(0);

        Runnable triggerSearch = () -> {
            String term = searchField.getText();
            onSearch.accept(term);
            clearButton.setOpacity(term.isBlank() ? 0 : 1);
        };

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) triggerSearch.run();
        });
        searchButton.setOnAction(event -> triggerSearch.run());
        clearButton.setOnAction(event -> {
            searchField.clear();
            onSearch.accept("");
            clearButton.setOpacity(0);
        });

        HBox searchInput = new HBox(8, iconLabel, searchField);
        searchInput.getStyleClass().add("search-bar");
        HBox.setHgrow(searchInput, Priority.ALWAYS);

        HBox actionButtons = new HBox(8, searchButton, clearButton);

        HBox content = new HBox(12, searchInput, actionButtons);
        content.getStyleClass().add("search-row");
        getChildren().add(content);

        updateTexts();
        LanguageManager.addObserver(this::updateTexts);
    }

    /**
     * Updates the visible text from the active language.
     */
    private void updateTexts() {
        searchField.setPromptText(LanguageManager.get(placeholderKey));
        searchButton.setText(LanguageManager.get(buttonKey));
    }
}
