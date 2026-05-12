package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;
import javafx.scene.layout.VBox;

/**
 * Search bar card component for the stocks tab.
 *
 * <p>Assembles a magnifying glass icon, a text field, and a search button
 * into a single row. Search is triggered explicitly
 * by pressing Enter or clicking the search button.
 */
public class StocksSearchBar extends VBox {

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button();

    /**
     * Constructs a new StocksSearchBar.
     *
     * @param gameService the game service used for observer registration
     * @param onSearch    callback invoked with the current search term when the user
     *                    presses Enter or clicks the search button
     */
    public StocksSearchBar(GameService gameService, Consumer<String> onSearch) {
        Label iconLabel = new Label("🔍");
        iconLabel.getStyleClass().add("search-icon");

        searchField.setPromptText(LanguageManager.get("exchange.stocks.search.placeholder"));
        //TODO: Connect to a Css file, searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchButton.setText(LanguageManager.get("exchange.stocks.search.button"));
       //TODO: Connect to a Css file,  searchButton.getStyleClass().add("search-button");

        Runnable trigger = () -> onSearch.accept(searchField.getText());
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) trigger.run();
        });
        searchButton.setOnAction(e -> trigger.run());

        getChildren().addAll(new HBox(8, iconLabel, searchField, searchButton));
    }
}
