package com.loopers.support.ranking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RankingKeyGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String PREFIX = "ranking:all:";

    public static String dailyKey(LocalDate date) {
        return PREFIX + date.format(FORMATTER);
    }

    public static long ttlSeconds() {
        return 2 * 24 * 60 * 60;
    }
}
