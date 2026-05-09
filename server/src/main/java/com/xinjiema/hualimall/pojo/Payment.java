package com.xinjiema.hualimall.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
    private String paymentNo;
    private Long orderId;
    private String channel; // ALIPAY | WECHAT
    private String status;  // PENDING(待处理 / 挂起) | PROCESSING(处理中) | SUCCESS (成功 / 完成)| CLOSED (已关闭)| FAILED (失败)
    private BigDecimal amount;
    private String payUrl;
    private String codeUrl;
    private String channelTradeNo;
    private OffsetDateTime expireTime;
    private OffsetDateTime paidTime;
    private OffsetDateTime closeTime;
    private String failReason;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}