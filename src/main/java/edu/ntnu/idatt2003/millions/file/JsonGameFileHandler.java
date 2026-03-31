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
 */
public class JsonGameFileHandler implements GameFileHandler {

    private final Gson gson;

    public JsonGameFileHandler() {
        this.gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Transaction.class, new TransactionSerializer())
                .setPrettyPrinting()
                .create();
    }

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
     * Custom serializer for Transaction so that the type (PURCHASE or SALE)
     * is included in the JSON, making it possible to deserialize correctly later.
     */
    private static class TransactionSerializer implements JsonSerializer<Transaction> {
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