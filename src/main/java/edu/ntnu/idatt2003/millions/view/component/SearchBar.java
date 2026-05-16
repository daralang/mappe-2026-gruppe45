package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.SearchFocusProvider;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Reusable search bar with icon, text field, search button, and clear button.
 *
 * <p>Search is triggered by Enter or clicking the button. ESC clears and blurs the field.
 * The {@code Cmd/Ctrl+F} shortcut to focus this bar is registered externally via
 * {@link SearchFocusProvider} and the application-wide
 * {@code KeyboardNavigationService}, keeping shortcut registration out of the view layer.</p>
 */
public class SearchBar extends VBox {

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button();
    private final Button clearButton = new Button();

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
        this(placeholderKey, buttonKey, onSearch, null);
    }

    /**
     * Creates a search bar with localized text and optional metadata below the input row.
     *
     * @param placeholderKey the i18n key for the field placeholder
     * @param buttonKey      the i18n key for the button text
     * @param onSearch       callback receiving the current search term on each
     *                       triggered search or clear action
     * @param metadata       optional metadata shown below the search row
     * @throws NullPointerException if placeholder key, button key or callback is null
     */
    public SearchBar(String placeholderKey, String buttonKey, Consumer<String> onSearch, Node metadata) {
        Objects.requireNonNull(placeholderKey, "Placeholder key cannot be null");
        Objects.requireNonNull(buttonKey, "Button key cannot be null");
        Objects.requireNonNull(onSearch, "Search callback cannot be null");

        this.placeholderKey = placeholderKey;
        this.buttonKey = buttonKey;

        FontIcon iconLabel = new FontIcon("fth-search");
        iconLabel.getStyleClass().add("search-icon");

        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchButton.getStyleClass().add("search-button");

        FontIcon clearIcon = new FontIcon("fth-x");
        clearIcon.getStyleClass().add("search-clear-icon");
        clearButton.setGraphic(clearIcon);
        clearButton.getStyleClass().add("search-clear-button");
        setClearVisible(false);

        Runnable triggerSearch = () -> {
            String term = searchField.getText();
            onSearch.accept(term);
            setClearVisible(!term.isBlank());
        };

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                triggerSearch.run();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                searchField.clear();
                onSearch.accept("");
                setClearVisible(false);
                searchField.getParent().requestFocus();
                event.consume();
            }
        });

        searchButton.setOnAction(event -> triggerSearch.run());
        clearButton.setOnAction(event -> {
            searchField.clear();
            onSearch.accept("");
            setClearVisible(false);
        });

        HBox.setHgrow(searchField, Priority.ALWAYS);

        HBox searchInput = new HBox(4, iconLabel, searchField, clearButton);
        searchInput.getStyleClass().add("search-bar");
        HBox.setHgrow(searchInput, Priority.ALWAYS);

        HBox content = new HBox(8, searchInput, searchButton);
        content.getStyleClass().add("search-row");
        getChildren().add(content);
        if (metadata != null) {
            metadata.getStyleClass().add("search-metadata");
            getChildren().add(metadata);
        }

        updateTexts();
        LanguageManager.addObserver(this::updateTexts);
    }

    /**
     * Requests keyboard focus on the search text field.
     * Call this from a {@link edu.ntnu.idatt2003.millions.view.SearchFocusProvider}
     * implementation when {@code Cmd/Ctrl+F} is pressed.
     */
    public void focus() {
        searchField.requestFocus();
    }

    /**
     * Shows or hides the clear button and keeps its tab-order participation in sync.
     *
     * <p>Opacity alone does not remove a node from tab traversal, so setFocusTraversable(boolean)
     * must be updated alongside the opacity to prevent the invisible button from receiving keyboard focus.</p>
     *
     * @param visible {@code true} to show the button and include it in tab order,
     *                {@code false} to hide it and exclude it
     */
    private void setClearVisible(boolean visible) {
        clearButton.setOpacity(visible ? 1 : 0);
        clearButton.setFocusTraversable(visible);
    }

    /**
     * Updates the visible text from the active language.
     */
    private void updateTexts() {
        searchField.setPromptText(LanguageManager.get(placeholderKey));
        searchButton.setText(LanguageManager.get(buttonKey));
    }
}
