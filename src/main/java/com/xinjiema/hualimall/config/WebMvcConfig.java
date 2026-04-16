package com.xinjiema.hualimall.config;

import com.xinjiema.hualimall.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/orders/**",
                        "/order/**",
                        "/carts/**",
                        "/cart/**",
                        "/admin/**",
                        "/users/me",
                        "/users/info",
                        "/user/me",
                        "/user/info"
                )
                .excludePathPatterns(
                        "/users",
                        "/users/register",
                        "/users/sessions",
                        "/user/register",
                        "/user/login"
                );
    }
}
