package com.xinjiema.hualimall.utils;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

public class JwtUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SECRET = resolveSecret();
    private static final long EXPIRE_SECONDS = 7L * 24 * 60 * 60;
    private static final String ISSUER = "hualimall";

    private JwtUtils() {
    }

    public static String createToken(Long userId, String username, String role) {
        try {
            if (userId == null || userId < 1) {
                throw new IllegalArgumentException("用户ID非法");
            }
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("用户名不能为空");
            }
            if (role == null || role.trim().isEmpty()) {
                throw new IllegalArgumentException("用户角色不能为空");
            }
            String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
            String header = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
            long now = Instant.now().getEpochSecond();
            long exp = now + EXPIRE_SECONDS;
            String payloadJson = OBJECT_MAPPER.writeValueAsString(Map.of(
                    "sub", userId,
                    "username", username,
                    "role", normalizedRole,
                    "iss", ISSUER,
                    "iat", now,
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
            if (token == null || token.trim().isEmpty()) {
                throw new SecurityException("token不能为空");
            }
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) {
                throw new SecurityException("token格式不正确");
            }
            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = hmacSha256(unsignedToken, SECRET);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new SecurityException("token签名不正确");
            }

            if (parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) {
                throw new SecurityException("token格式不正确");
            }
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = OBJECT_MAPPER.readValue(payloadJson, new TypeReference<>() {
            });

            String issuer = String.valueOf(claims.get("iss"));
            if (!ISSUER.equals(issuer)) {
                throw new SecurityException("token签发者不正确");
            }

            Object expObject = claims.get("exp");
            if (expObject == null) {
                throw new SecurityException("token缺少过期时间");
            }
            long exp = Long.parseLong(String.valueOf(expObject));
            if (Instant.now().getEpochSecond() > exp) {
                throw new SecurityException("token已过期");
            }

            Object subObject = claims.get("sub");
            if (subObject == null) {
                throw new SecurityException("token缺少用户信息");
            }
            Long userId = Long.parseLong(String.valueOf(subObject));

            String username = String.valueOf(claims.get("username"));
            if (username == null || username.trim().isEmpty() || "null".equals(username)) {
                throw new SecurityException("token缺少用户名");
            }
            String role = String.valueOf(claims.get("role"));
            if (role == null || role.trim().isEmpty() || "null".equals(role)) {
                throw new SecurityException("token缺少角色信息");
            }
            return new Claims(userId, username, role.trim().toUpperCase(Locale.ROOT));
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

    private static byte[] base64UrlDecode(String content) {
        try {
            return Base64.getUrlDecoder().decode(content);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("token编码不正确");
        }
    }

    private static String resolveSecret() {
        String fromProperty = System.getProperty("jwt.secret");
        if (fromProperty != null && !fromProperty.trim().isEmpty()) {
            return fromProperty.trim();
        }
        String fromEnv = System.getenv("JWT_SECRET");
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        return "hualimall-secret-key-change-me";
    }

    public record Claims(Long userId, String username, String role) {
    }
}
