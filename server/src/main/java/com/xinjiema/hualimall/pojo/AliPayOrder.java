package com.xinjiema.hualimall.pojo;

import lombok.Data;

/**
 * 支付请求参数实体
 */
@Data
public class AliPayOrder {
    /**
     * 商户订单号（你自己生成的唯一订单编号）
     */
    private String traceNo;

    /**
     * 订单总金额（单位：元）
     */
    private double totalAmount;

    /**
     * 商品名称 / 订单标题
     */
    private String subject;

    /**
     * 支付宝交易流水号（退款时用）
     */
    private String alipayTraceNo;
}