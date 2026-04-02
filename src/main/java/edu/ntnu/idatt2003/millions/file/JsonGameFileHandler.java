package edu.ntnu.idatt2003.millions.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import edu.ntnu.idatt2003.millions.model.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Handles reading and writing of game state to and from JSON files.
 * Uses Gson for serialization and deserialization.
 * Transactions are serialized with a type field to distinguish
 * between purchases and sales when loading the game back.
 */
public class JsonGameFileHandler implements GameFileHandler {

    private final Gson gson;

    /**
     * Constructs a new JsonGameFileHandler.
     * Configures Gson with a custom serializer for transactions
     * and pretty printing for human-readable output.
     */
    public JsonGameFileHandler() {
        this.gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Transaction.class, new TransactionSerializer())
                .setPrettyPrinting()
                .create();
    }

    /**
     * Saves the current game state to a JSON file.
     * The file will contain the player state (money, portfolio,
     * transaction archive) and the exchange state (stocks, prices, week).
     *
     * @param player   the player whose state should be saved
     * @param exchange the exchange whose state should be saved
     * @param file     the file to save the game state to
     * @throws UncheckedIOException if the file cannot be written to
     */
    @Override
    public void saveGame(Player player, Exchange exchange, File file) {
        JsonObject gameState = new JsonObject();
        gameState.add("player", gson.toJsonTree(player));
        gameState.add("exchange", gson.toJsonTree(exchange));

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(gameState, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save game to file: " + file.getName(), e);
        }
    }

    /**
     * Loads a saved game state from a JSON file.
     * Deserializes the player and exchange from the file, then relinks
     * each share in the player's portfolio to the correct stock reference
     * from the exchange.
     *
     * @param file the file to load the game state from
     * @return a {@link GameState} containing the deserialized player and exchange
     * @throws NullPointerException if the file is null
     * @throws UncheckedIOException if the file cannot be read
     */
    @Override
    public GameState loadGame(File file) {
        try (FileReader reader = new FileReader(file)) {
            JsonObject gameState = gson.fromJson(reader, JsonObject.class);

            Exchange exchange = gson.fromJson(gameState.get("exchange"), Exchange.class);
            Player player = gson.fromJson(gameState.get("player"), Player.class);

            relinkShares(player, exchange);

            return new GameState(player, exchange);

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load game from file: " + file.getName(), e);
        }
    }

    /**
     * Custom serializer for Transaction that adds a type field
     * to the JSON output to distinguish between purchases and sales.
     * This is necessary because Transaction is abstract, and Gson
     * needs to know which subclass to instantiate when deserializing.
     */
    private static class TransactionSerializer implements JsonSerializer<Transaction> {

        /**
         * Serializes a transaction to a JSON object, adding a type field
         * with the value "PURCHASE" or "SALE".
         *
         * @param transaction the transaction to serialize
         * @param type        the type of the transaction
         * @param context     the serialization context
         * @return the serialized JSON element
         */
        @Override
        public JsonElement serialize(Transaction transaction, Type type,
                                     JsonSerializationContext context) {
            JsonObject obj = context.serialize(transaction,
                    transaction.getClass()).getAsJsonObject();
            obj.addProperty("type",
                    transaction instanceof Purchase ? "PURCHASE" : "SALE");
            return obj;
        }
    }


    /**
     * Relinks each share in the player's portfolio to the correct
     * Stock object from the exchange after deserialization.
     * This is necessary because Gson creates new Stock objects
     * instead of reusing the ones in the exchange.
     *
     * @param player   the player whose portfolio should be relinked
     * @param exchange the exchange containing the correct stock references
     */
    private void relinkShares(Player player, Exchange exchange) {
        List<Share> relinked = player.getPortfolio().getShares().stream()
                .map(share -> new Share(
                        exchange.getStock(share.getStock().getSymbol()),
                        share.getQuantity(),
                        share.getPurchasePrice()
                ))
                .toList();

        player.getPortfolio().setShares(relinked);
    }
}