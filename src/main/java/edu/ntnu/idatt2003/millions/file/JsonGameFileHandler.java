package edu.ntnu.idatt2003.millions.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import edu.ntnu.idatt2003.millions.model.Exchange;
import edu.ntnu.idatt2003.millions.model.Player;
import edu.ntnu.idatt2003.millions.model.Purchase;
import edu.ntnu.idatt2003.millions.model.Transaction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

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

    @Override
    public void loadGame(File file) {
        // TODO: implementeres senere
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
}