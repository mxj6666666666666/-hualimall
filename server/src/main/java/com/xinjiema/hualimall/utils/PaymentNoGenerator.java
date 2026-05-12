package com.xinjiema.hualimall.utils;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class PaymentNoGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generate() {
        byte[] randomBytes = new byte[8];
        SECURE_RANDOM.nextBytes(randomBytes);
        long epochMillis = Instant.now().toEpochMilli();
        String timeHex = Long.toHexString(epochMillis).toUpperCase();
        String randomHex = HexFormat.of().formatHex(randomBytes).toUpperCase();
        String uuidHex = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "P" + timeHex + randomHex + uuidHex.substring(0, 12);
    }

    public String generateUuidStyle() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        UUID uuid = UUID.nameUUIDFromBytes(ByteBuffer.wrap(bytes).array());
        return "P" + uuid.toString().replace("-", "").toUpperCase();
    }
}

