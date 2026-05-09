package com.xinjiema.hualimall.pojo;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    private Long orderId;
    private String channel; // ALIPAY | WECHAT
}