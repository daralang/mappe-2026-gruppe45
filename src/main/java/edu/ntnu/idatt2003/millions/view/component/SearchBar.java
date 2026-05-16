package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.keyboard.KeyBinding;
import edu.ntnu.idatt2003.millions.keyboard.KeyBinding.Modifier;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
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
 * <p>Search is triggered by Enter or clicking the button. Cmd/Ctrl+F focuses
 * the field from anywhere in the scene; ESC clears and blurs it.</p>
 */
public class SearchBar extends VBox {

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button();
    private final Button clearButton = new Button();
    private static final KeyBinding FOCUS_BINDING = KeyBinding.of(KeyCode.F, Modifier.SHORTCUT);

    private final String placeholderKey;
    private final String buttonKey;
    private final javafx.event.EventHandler<KeyEvent> sceneKeyHandler = event -> {
        if (FOCUS_BINDING.matches(event)) {
            searchField.requestFocus();
            event.consume();
        }
    };

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
        clearButton.setOpacity(0);

        Runnable triggerSearch = () -> {
            String term = searchField.getText();
            onSearch.accept(term);
            clearButton.setOpacity(term.isBlank() ? 0 : 1);
        };

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                triggerSearch.run();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                searchField.clear();
                onSearch.accept("");
                clearButton.setOpacity(0);
                searchField.getParent().requestFocus();
                event.consume();
            }
        });

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, sceneKeyHandler);
            if (newScene != null) newScene.addEventFilter(KeyEvent.KEY_PRESSED, sceneKeyHandler);
        });
        searchButton.setOnAction(event -> triggerSearch.run());
        clearButton.setOnAction(event -> {
            searchField.clear();
            onSearch.accept("");
            clearButton.setOpacity(0);
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
     * Updates the visible text from the active language.
     */
    private void updateTexts() {
        searchField.setPromptText(LanguageManager.get(placeholderKey));
        searchButton.setText(LanguageManager.get(buttonKey));
    }
}
