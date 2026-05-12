package com.xinjiema.hualimall.service;

import com.alipay.api.AlipayApiException;
import com.xinjiema.hualimall.pojo.CreatePaymentRequest;
import com.xinjiema.hualimall.pojo.Payment;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    // 【修改】Service 层不再接收 HttpServletResponse，只返回业务数据
    Payment createPayment(CreatePaymentRequest req, Long currentUserId, String currentRole);

    // 【新增】构建支付宝表单 HTML，由 Controller 决定如何输出
    String buildAlipayForm(String paymentNo, Long currentUserId, String currentRole) throws AlipayApiException;

    Payment getById(Long id, Long currentUserId, String currentRole);

    Payment getLatestByOrderId(Long orderId, Long currentUserId, String currentRole);

    void closePayment(Long id, Long currentUserId, String currentRole);

    String handleAlipayNotify(HttpServletRequest request) throws AlipayApiException;

    String handleWechatNotify(String body);

    void reconcileTimeoutPayments();
}
