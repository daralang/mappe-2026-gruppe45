package edu.ntnu.idatt2003.millions.file.game;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles reading and writing of game state to and from JSON files.
 * Uses Gson for serialization and deserialization.
 * Transactions are serialized with a type field to distinguish
 * between purchases and sales when loading the game back.
 *
 * <p>This class is pure infrastructure: it does not own domain decisions,
 * such as which {@link edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter}
 * implementation to use. Callers (typically
 * {@link edu.ntnu.idatt2003.millions.service.GameService}) are responsible for
 * reinitializing the loaded {@link Exchange} with a converter via
 * {@link Exchange#reinitialize} before it is used.
 */
public class JsonGameFileHandler implements GameFileHandler {

    private final Gson gson;

    /**
     * Constructs a new JsonGameFileHandler.
     * Configures Gson with a custom serializer for transactions
     * and pretty printing for readable output.
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
     * @throws NullPointerException if player, exchange or file is null
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
     * <p>The returned {@link Exchange} has not yet had its transient fields
     * (random source, currency converter) populated. Callers must invoke
     * {@link Exchange#reinitialize} before using the exchange.
     *
     * @param file the file to load the game state from
     * @return a {@link GameState} containing the deserialized player and exchange
     * @throws NullPointerException     if the file is null
     * @throws UncheckedIOException     if the file cannot be read
     * @throws GameSaveCorruptException if the file is not valid JSON or is missing
     *                                  required top-level fields
     */
    @Override
    public GameState loadGame(File file) throws GameSaveCorruptException {
        Objects.requireNonNull(file, "File cannot be null");

        try (FileReader reader = new FileReader(file)) {
            JsonObject gameState;
            try {
                gameState = gson.fromJson(reader, JsonObject.class);
            } catch (JsonParseException e) {
                throw new GameSaveCorruptException(
                        "Save file contains invalid JSON: " + file.getName(), e);
            }

            if (gameState == null
                    || !gameState.has("player")
                    || !gameState.has("exchange")) {
                throw new GameSaveCorruptException(
                        "Save file is missing required fields 'player' or 'exchange': " + file.getName());
            }

            Exchange exchange;
            Player player;
            try {
                exchange = gson.fromJson(gameState.get("exchange"), Exchange.class);
                player = gson.fromJson(gameState.get("player"), Player.class);
            } catch (JsonParseException e) {
                throw new GameSaveCorruptException(
                        "Save file has an unreadable structure: " + file.getName(), e);
            }

            relinkShares(player, exchange);
            mergeSharesBySymbol(player);
            relinkArchive(player, exchange);

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
     * Relinks each share in every archived transaction to the canonical
     * Stock object from the exchange after deserialization.
     * Gson creates fresh Stock instances per JSON object; without relinking,
     * archived transactions hold orphan Stock copies that diverge from the
     * exchange's live instances.
     *
     * @param player   the player whose transaction archive should be relinked
     * @param exchange the exchange containing the correct stock references
     */
    private void relinkArchive(Player player, Exchange exchange) {
        for (Transaction transaction : player.getTransactionArchive().getAll()) {
            Share oldShare = transaction.getShare();
            var canonicalStock = exchange.getStock(oldShare.getStock().getSymbol());
            if (canonicalStock != null) {
                transaction.relinkShare(new Share(canonicalStock, oldShare.getQuantity(), oldShare.getPurchasePrice()));
            }
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

    /**
     * Consolidates any duplicate-symbol shares in the player's portfolio using
     * weighted-average purchase price (GAV). Called after {@link #relinkShares}
     * to enforce the one-share-per-symbol invariant on legacy save files.
     *
     * @param player the player whose portfolio should be consolidated
     */
    private void mergeSharesBySymbol(Player player) {
        List<Share> merged = new ArrayList<>();
        for (Share share : player.getPortfolio().getShares()) {
            String symbol = share.getStock().getSymbol();
            boolean found = false;
            for (int i = 0; i < merged.size(); i++) {
                if (merged.get(i).getStock().getSymbol().equals(symbol)) {
                    merged.set(i, merged.get(i).mergedWith(share));
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(share);
            }
        }
        player.getPortfolio().setShares(merged);
    }
}