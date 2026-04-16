package com.xinjiema.hualimall.interceptor;

import com.xinjiema.hualimall.utils.AuthContext;
import com.xinjiema.hualimall.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("请先登录");
        }
        String token = authHeader.substring("Bearer ".length());
        JwtUtils.Claims claims = JwtUtils.parseToken(token);
        AuthContext.setCurrentUser(claims.userId(), claims.username());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
