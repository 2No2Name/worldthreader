package no2.worldthreader.common.scoreboard;

public interface ThreadsafeScoreboard {

    void worldthreader$ensureExclusiveScoreboardAccess();

    void worldthreader$crashIfNoExclusiveScoreboardAccess();
}
