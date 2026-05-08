package com.xinjiema.hualimall.pojo;

import java.math.BigDecimal;

public class Payment {

    private Long id;

    /**
     * 支付流水号
     * 系统内部生成的唯一支付编号，用于追踪和标识每一笔支付交易
     * 格式示例：PAY20231225001
     */
    private String paymentNo;

    private Long orderId;

    /**
     * 支付渠道
     * - ALIPAY: 支付宝支付
     * - WECHAT: 微信支付
     */
    private String channel;

    /**
     * 支付状态
     * 记录当前支付的处理状态，如：
     * - UNPAID: 待支付
     * - PAID: 已支付
     * - REFUNDING: 退款中
     * - REFUNDED: 已退款
     * - CLOSED: 已关闭
     * - FAILED: 支付失败
     */
    private String status;

    private BigDecimal amount;

    /**
     * 支付链接
     * 第三方支付平台（如支付宝）返回的支付页面URL
     * 用户可通过此链接完成支付操作
     * 可为null（仅当支付尚未创建时）
     */
    private String payUrl;
}