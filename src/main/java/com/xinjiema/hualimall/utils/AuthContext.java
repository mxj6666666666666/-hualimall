package com.xinjiema.hualimall.utils;

public class AuthContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_ROLE = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setCurrentUser(Long userId, String username, String role) {
        CURRENT_USER_ID.set(userId);
        CURRENT_USERNAME.set(username);
        CURRENT_USER_ROLE.set(role);
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static Long requireCurrentUserId() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null || userId < 1) {
            throw new SecurityException("登录状态无效，请重新登录");
        }
        return userId;
    }

    public static String getCurrentUsername() {
        return CURRENT_USERNAME.get();
    }

    public static String requireCurrentUsername() {
        String username = CURRENT_USERNAME.get();
        if (username == null || username.trim().isEmpty()) {
            throw new SecurityException("登录状态无效，请重新登录");
        }
        return username;
    }

    public static String getCurrentUserRole() {
        return CURRENT_USER_ROLE.get();
    }

    public static String requireCurrentUserRole() {
        String role = CURRENT_USER_ROLE.get();
        if (role == null || role.trim().isEmpty()) {
            throw new SecurityException("登录状态无效，请重新登录");
        }
        return role;
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_USERNAME.remove();
        CURRENT_USER_ROLE.remove();
    }
}
