package com.xinjiema.hualimall.interceptor;

import com.xinjiema.hualimall.utils.AuthContext;
import com.xinjiema.hualimall.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("请先登录");
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new SecurityException("token不能为空");
        }

        JwtUtils.Claims claims = JwtUtils.parseToken(token);
        String requestUri = request.getRequestURI();
        if (isAdminPath(requestUri) && !"ADMIN".equals(claims.role())) {
            throw new SecurityException("无管理员权限");
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
}
