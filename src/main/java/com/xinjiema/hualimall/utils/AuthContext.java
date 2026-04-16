package com.xinjiema.hualimall.utils;

public class AuthContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setCurrentUser(Long userId, String username) {
        CURRENT_USER_ID.set(userId);
        CURRENT_USERNAME.set(username);
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static String getCurrentUsername() {
        return CURRENT_USERNAME.get();
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_USERNAME.remove();
    }
}
