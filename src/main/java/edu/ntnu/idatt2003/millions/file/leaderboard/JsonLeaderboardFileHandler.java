// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.file.leaderboard;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.ntnu.idatt2003.millions.model.leaderboard.LeaderboardEntry;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JSON-backed leaderboard storage with atomic writes.
 *
 * <p>Writes are atomic: the new content is first written to a sibling
 * {@code .tmp} file, then moved over the target file. On the same filesystem
 * this rename is atomic, so the target either holds the previous full content
 * or the new full content — never a half-written mix even if the JVM dies
 * mid-write.</p>
 *
 * <p>This class is pure infrastructure — it does not enforce ranking rules,
 * upsert semantics, or any other business logic. The
 * {@link edu.ntnu.idatt2003.millions.service.LeaderboardService} owns those.</p>
 */
public class JsonLeaderboardFileHandler implements LeaderboardFileHandler {

    private final Gson gson;

    public JsonLeaderboardFileHandler() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .setPrettyPrinting()
                .create();
    }

    /**
     * Reads all leaderboard entries from the given JSON file. Returns an empty
     * list if the file does not exist — that is the expected state on the very
     * first run.
     *
     * @param file the file to read from
     * @return list of entries; empty if the file is missing or contains no entries
     * @throws LeaderboardCorruptException if the file exists but cannot be parsed
     */
    @Override
    public List<LeaderboardEntry> readAll(File file) throws LeaderboardCorruptException {
        Objects.requireNonNull(file, "File cannot be null");
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            return parse(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to read leaderboard file: " + file.getName(), e);
        }
    }

    /**
     * Parses leaderboard entries from an already-opened reader.
     *
     * @param reader the reader positioned at the start of a JSON leaderboard array
     * @return a mutable list of entries; empty if the JSON is {@code null} or {@code []}
     * @throws LeaderboardCorruptException if the input is not valid JSON
     */
    List<LeaderboardEntry> parse(Reader reader) throws LeaderboardCorruptException {
        Type listType = new TypeToken<List<LeaderboardEntry>>() {}.getType();
        try {
            List<LeaderboardEntry> entries = gson.fromJson(reader, listType);
            return entries == null ? new ArrayList<>() : new ArrayList<>(entries);
        } catch (JsonParseException e) {
            throw new LeaderboardCorruptException("Leaderboard JSON is corrupt", e);
        }
    }

    /**
     * Writes all entries to the given JSON file atomically.
     *
     * <p>If the file's parent directory does not exist, it is created. Content
     * is first written to a {@code .tmp} sibling, then moved over the target
     * file using an atomic move when supported by the OS.</p>
     *
     * @param entries the entries to persist
     * @param file    the file to write to
     * @throws UncheckedIOException if the file cannot be written
     */
    @Override
    public void writeAll(List<LeaderboardEntry> entries, File file) {
        Objects.requireNonNull(entries, "Entries cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

        Path target = file.toPath().toAbsolutePath();
        Path parent = target.getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
            try (FileWriter writer = new FileWriter(tmp.toFile())) {
                gson.toJson(entries, writer);
            }
            moveAtomicallyIfSupported(tmp, target);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to write leaderboard file: " + file.getName(), e);
        }
    }

    private static void moveAtomicallyIfSupported(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (UnsupportedOperationException _) {
            // Fall back to non-atomic move on file systems that don't support it.
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Reads and writes {@link Instant} as an ISO-8601 string. Gson does not
     * handle {@code java.time} types natively.
     */
    private static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return Instant.parse(json.getAsString());
            } catch (Exception e) {
                throw new JsonParseException("Invalid Instant: " + json.getAsString(), e);
            }
        }
    }
}