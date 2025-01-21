package no2.worldthreader.common.scoreboard;

public interface AtomicArithmeticScore {
    int worldthreader$addToValueAndGet(int amount);
    int worldthreader$compareExchangeValue(int value, int expected);
}
