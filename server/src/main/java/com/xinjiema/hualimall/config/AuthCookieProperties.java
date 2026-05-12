package com.xinjiema.hualimall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.cookie")
public class AuthCookieProperties {
    private String name = "HM_TOKEN";
    private long maxAgeSeconds = 7L * 24 * 60 * 60;
    private boolean secure = true;
    private String sameSite = "Lax";
}

