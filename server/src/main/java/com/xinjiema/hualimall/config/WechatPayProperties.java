package com.xinjiema.hualimall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat-pay")
public class WechatPayProperties {
    private String appId;
    private String mchId;
    private String apiV2Key;
}

