package com.xinjiema.hualimall.config;

import com.xinjiema.hualimall.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/orders/**",
                        "/order/**",
                        "/carts/**",
                        "/cart/**",
                        "/admin/**",
                        "/merchant/**",
                        "/users/me",
                        "/users/info",
                        "/user/me",
                        "/user/info"
                )
                .excludePathPatterns(
                        "/users",
                        "/users/register",
                        "/users/login",
                        "/users/sessions",
                        "/user/register",
                        "/user/login"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
