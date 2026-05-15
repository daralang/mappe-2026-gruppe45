# Leaderboard

Group-shared scoreboard stored at `./leaderboard.json` (project root).
Each game session is identified by a UUID generated when the player is
created and preserved across save/load, so a single session only ever
takes up one row no matter how many times you save.

## How sharing works

The file is committed to git. To see other group members' scores:

    git pull

To share your own scores after playing:

    git add leaderboard.json
    git commit -m "Update leaderboard"
    git push

## Avoiding merge conflicts

If two of you play at the same time and both push, git will report a
conflict because both commits touched the same JSON file. The conflict
is easy to resolve because each entry is keyed by a UUID — you just
keep both:

1. `git pull` will fail with a merge conflict on `leaderboard.json`
2. Open the file in your editor; you'll see `<<<<<<<` markers
3. Keep the entries from both sides (they have different sessionIds,
   so there's no real collision — the conflict is purely textual)
4. Remove the conflict markers and save
5. `git add leaderboard.json && git commit && git push`

If you prefer to avoid the manual merge, coordinate via the group chat:
"I'm playing now, push when done."

## How entries are created

- **On save (`Save game`)**: an entry with status `ACTIVE` is created or
  updated for the current session.
- **On game over**: the entry is updated one last time with status
  `GAME_OVER` and locked. Subsequent saves in the same session
  (which can only happen via a stale UI state) are ignored.

## Ranking

Primary: return percent since start, descending.
Tiebreaker: final net worth in NOK, descending.

Players who lost their game (`GAME_OVER`) are still ranked alongside
everyone else; the UI flags them visually but does not push them down.

## Resetting the leaderboard

To wipe all scores during development:

    rm leaderboard.json

The file will be re-created on the next save with an empty list as its
starting point.

## If the file gets polluted by tests

If you ever see many entries with identical timestamps, all on week 1,
all with the same player name and starting capital, that's a sign a test
has accidentally written to the real `leaderboard.json` instead of a
temp file. `GameServiceTest` (and anything else that constructs a real
`GameService`) must use the dependency-injection constructor:

    LeaderboardService lb = new LeaderboardService(
            new JsonLeaderboardFileHandler(),
            tempDir.resolve("leaderboard.json").toFile());
    GameService gs = new GameService(lb);

Never `new GameService()` in a test — it points the leaderboard at the
project root.
