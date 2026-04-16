package com.xinjiema.hualimall.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

public class JwtUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SECRET = "hualimall-secret-key-change-me";
    private static final long EXPIRE_SECONDS = 7L * 24 * 60 * 60;

    private JwtUtils() {
    }

    public static String createToken(Long userId, String username) {
        try {
            String header = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
            long exp = Instant.now().getEpochSecond() + EXPIRE_SECONDS;
            String payloadJson = OBJECT_MAPPER.writeValueAsString(Map.of(
                    "sub", userId,
                    "username", username,
                    "exp", exp
            ));
            String payload = base64UrlEncode(payloadJson);
            String unsignedToken = header + "." + payload;
            String signature = hmacSha256(unsignedToken, SECRET);
            return unsignedToken + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("生成token失败", e);
        }
    }

    public static Claims parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new SecurityException("token格式不正确");
            }
            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = hmacSha256(unsignedToken, SECRET);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new SecurityException("token签名不正确");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = OBJECT_MAPPER.readValue(payloadJson, new TypeReference<>() {
            });

            Object expObject = claims.get("exp");
            long exp = Long.parseLong(String.valueOf(expObject));
            if (Instant.now().getEpochSecond() > exp) {
                throw new SecurityException("token已过期");
            }

            Long userId = Long.parseLong(String.valueOf(claims.get("sub")));
            String username = String.valueOf(claims.get("username"));
            return new Claims(userId, username);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("token解析失败");
        }
    }

    private static String base64UrlEncode(String content) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    private static String hmacSha256(String content, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signBytes);
    }

    public record Claims(Long userId, String username) {
    }
}
