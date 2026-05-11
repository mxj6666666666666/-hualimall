package com.xinjiema.hualimall.controll;

import com.alipay.api.AlipayApiException;
import com.xinjiema.hualimall.pojo.CreatePaymentRequest;
import com.xinjiema.hualimall.pojo.Payment;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    // 【修改】createPayment 仅创建支付单并返回业务数据，不再直接输出 HTML
    public Result<Payment> createPayment(@RequestBody CreatePaymentRequest req) {
        log.info("创建支付单: {}", req);
        return Result.success(paymentService.createPayment(req));
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
