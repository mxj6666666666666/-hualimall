package com.xinjiema.hualimall.utils;

import java.security.SecureRandom;
import java.util.UUID;

public final class UuidV7Utils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7Utils() {
    }

    public static String generate() {
        long timestampMs = System.currentTimeMillis() & 0xFFFFFFFFFFFFL;
        long randomA = RANDOM.nextInt(1 << 12) & 0xFFFL;
        long randomB = RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL;

        long mostSignificantBits = (timestampMs << 16) | (0x7L << 12) | randomA;
        long leastSignificantBits = (0x2L << 62) | randomB;

        return new UUID(mostSignificantBits, leastSignificantBits).toString();
    }
}
