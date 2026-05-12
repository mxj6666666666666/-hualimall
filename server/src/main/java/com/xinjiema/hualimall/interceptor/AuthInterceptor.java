package com.xinjiema.hualimall.interceptor;

import com.xinjiema.hualimall.config.AuthCookieProperties;
import com.xinjiema.hualimall.utils.AuthContext;
import com.xinjiema.hualimall.utils.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthCookieProperties authCookieProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = readTokenFromCookie(request);
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring("Bearer ".length()).trim();
            }
        }
        if (token == null || token.isEmpty()) {
            throw new SecurityException("token不能为空");
        }

        JwtUtils.Claims claims = jwtUtils.parseToken(token);
        String requestUri = request.getRequestURI();
        if (isAdminPath(requestUri) && !"ADMIN".equals(claims.role())) {
            throw new SecurityException("无管理员权限");
        }
        if (isMerchantPath(requestUri) && !"MERCHANT".equals(claims.role())) {
            throw new SecurityException("无商家权限");
        }

        AuthContext.setCurrentUser(claims.userId(), claims.username(), claims.role());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean isAdminPath(String requestUri) {
        if (requestUri == null) {
            return false;
        }
        String normalizedPath = requestUri.toLowerCase(Locale.ROOT);
        return normalizedPath.equals("/admin") || normalizedPath.startsWith("/admin/");
    }

    private boolean isMerchantPath(String requestUri) {
        if (requestUri == null) {
            return false;
        }
        String normalizedPath = requestUri.toLowerCase(Locale.ROOT);
        return normalizedPath.equals("/merchant") || normalizedPath.startsWith("/merchant/");
    }

    private String readTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (authCookieProperties.getName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
