package com.xinjiema.hualimall.controll;

import com.alipay.api.AlipayApiException;
import com.xinjiema.hualimall.pojo.CreatePaymentRequest;
import com.xinjiema.hualimall.pojo.Payment;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Object createPayment(@RequestBody CreatePaymentRequest req, HttpServletResponse httpResponse) throws Exception {
        log.info("创建支付单: {}", req);
        Payment payment = paymentService.createPayment(req, httpResponse);
        if (httpResponse.isCommitted()) {
            return null;
        }
        return Result.success(payment);
    }

    @GetMapping("/{id}")
    public Result<Payment> getPaymentById(@PathVariable Long id) {
        return Result.success(paymentService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public Result<Payment> getPaymentByOrderId(@PathVariable Long orderId) {
        return Result.success(paymentService.getLatestByOrderId(orderId));
    }

    @PutMapping("/{id}/close")
    public Result<String> closePayment(@PathVariable Long id) {
        paymentService.closePayment(id);
        return Result.success("关闭成功");
    }

    // 回调接口
    @PostMapping("/notify/alipay")
    public String notifyAlipay(HttpServletRequest request) throws AlipayApiException {
        return paymentService.handleAlipayNotify(request);
    }

    @PostMapping("/notify/wechat")
    public String notifyWechat(@RequestBody String body) {
        return paymentService.handleWechatNotify(body);
    }
}
