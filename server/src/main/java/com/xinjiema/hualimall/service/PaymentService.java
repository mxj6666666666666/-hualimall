package com.xinjiema.hualimall.service;

import com.alipay.api.AlipayApiException;
import com.xinjiema.hualimall.pojo.CreatePaymentRequest;
import com.xinjiema.hualimall.pojo.Payment;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public interface PaymentService {
    Payment createPayment(CreatePaymentRequest req, HttpServletResponse httpResponse) throws IOException;

    Payment getById(Long id);

    Payment getLatestByOrderId(Long orderId);

    void closePayment(Long id);

    String handleAlipayNotify(Map<String, String> params) throws AlipayApiException;

    String handleWechatNotify(String body);

    void reconcileTimeoutPayments();
}
