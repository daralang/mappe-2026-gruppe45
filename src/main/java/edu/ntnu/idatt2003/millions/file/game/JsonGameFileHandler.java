package edu.ntnu.idatt2003.millions.file.game;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import edu.ntnu.idatt2003.millions.model.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

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
                .registerTypeAdapterFactory(new TransactionAdapterFactory())
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
     * @throws NullPointerException  if player, exchange or file is null
     * @throws UncheckedIOException if the file cannot be written to
     */
    @Override
    public void saveGame(Player player, Exchange exchange, File file) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(exchange, "Exchange cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

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
        Objects.requireNonNull(file, "File cannot be null");

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
     * Custom TypeAdapterFactory for Transaction that adds a type field
     * to the JSON output to distinguish between purchases and sales.
     * Uses getDelegateAdapter to avoid infinite recursion that occurs
     * with JsonSerializer when calling context.serialize().
     */
    private static class TransactionAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!Transaction.class.isAssignableFrom(type.getRawType())) {
                return null;
            }

            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    JsonObject obj = delegate.toJsonTree(value).getAsJsonObject();
                    obj.addProperty("type",
                            value instanceof Purchase ? "PURCHASE" : "SALE");
                    elementAdapter.write(out, obj);
                }

                @SuppressWarnings("unchecked")
                @Override
                public T read(JsonReader in) throws IOException {
                    JsonObject obj = elementAdapter.read(in).getAsJsonObject();
                    String type = obj.get("type").getAsString();

                    if ("PURCHASE".equals(type)) {
                        return (T) gson.getDelegateAdapter(
                                TransactionAdapterFactory.this,
                                TypeToken.get(Purchase.class)
                        ).fromJsonTree(obj);
                    } else {
                        return (T) gson.getDelegateAdapter(
                                TransactionAdapterFactory.this,
                                TypeToken.get(Sale.class)
                        ).fromJsonTree(obj);
                    }
                }
            };
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