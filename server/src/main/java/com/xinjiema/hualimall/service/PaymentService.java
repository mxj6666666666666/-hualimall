package com.xinjiema.hualimall.service;

import com.alipay.api.AlipayApiException;
import com.xinjiema.hualimall.pojo.CreatePaymentRequest;
import com.xinjiema.hualimall.pojo.Payment;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    // 【修改】Service 层不再接收 HttpServletResponse，只返回业务数据
    Payment createPayment(CreatePaymentRequest req);

    // 【新增】构建支付宝表单 HTML，由 Controller 决定如何输出
    String buildAlipayForm(String paymentNo) throws AlipayApiException;

    Payment getById(Long id);

    Payment getLatestByOrderId(Long orderId);

    void closePayment(Long id);

    String handleAlipayNotify(HttpServletRequest request) throws AlipayApiException;

    String handleWechatNotify(String body);

    void reconcileTimeoutPayments();
}
