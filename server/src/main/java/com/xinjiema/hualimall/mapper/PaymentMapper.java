package com.xinjiema.hualimall.mapper;

import com.xinjiema.hualimall.pojo.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface PaymentMapper {
    void insert(Payment payment);

    Payment selectById(@Param("id") Long id);

    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);

    Payment selectLatestByOrderId(@Param("orderId") Long orderId);

    // 【新增】回填/修复 pay_url，避免旧数据或异常流程导致支付链接为空
    int updatePayUrl(@Param("id") Long id, @Param("payUrl") String payUrl);

    /**
     * 查询订单下未终态的支付单（PENDING / PROCESSING），用于创建支付时的幂等性判断
     */
    Payment selectActiveByOrderId(@Param("orderId") Long orderId);

    /**
     * 将支付单状态改为 CLOSED，仅当当前状态为 PENDING 或 PROCESSING 才会生效
     * @return 受影响行数，为 0 表示状态已变更（并发保护）
     */
    int updateStatusToClosed(@Param("id") Long id,
                             @Param("closeTime") OffsetDateTime closeTime);

    /**
     * 将支付单状态改为 SUCCESS，同时记录渠道交易号与支付时间
     * @return 受影响行数
     */
    int updateStatusToSuccess(@Param("id") Long id,
                              @Param("channelTradeNo") String channelTradeNo,
                              @Param("paidTime") OffsetDateTime paidTime);

    /**
     * 将支付单状态改为 FAILED，记录失败原因
     * @return 受影响行数
     */
    int updateStatusToFailed(@Param("id") Long id,
                             @Param("failReason") String failReason);

    /**
     * 查询所有已过期且状态仍为 PENDING 的支付单
     */
    List<Payment> selectTimeoutPending(@Param("now") OffsetDateTime now);
}
