package com.xinjiema.hualimall.controll;

import com.alipay.api.AlipayApiException;
import com.xinjiema.hualimall.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/pay")
public class AlipayController {

    @Autowired
    private PaymentService paymentService;

    // 【新增】独立支付页：仅 Controller 操作 HttpServletResponse 输出支付宝表单
    @GetMapping("/alipay/{paymentNo}")
    public void openAlipayCashier(@PathVariable String paymentNo, HttpServletResponse response)
            throws AlipayApiException, IOException {
        // 【修改】修复 /pay/alipay/null 或 undefined 场景
        if (paymentNo == null || paymentNo.isBlank() || "null".equalsIgnoreCase(paymentNo) || "undefined".equalsIgnoreCase(paymentNo)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<html><body><h3>支付单号不能为空</h3></body></html>");
            response.getWriter().flush();
            return;
        }
        try {
            String formHtml = paymentService.buildAlipayForm(paymentNo);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(formHtml);
            response.getWriter().flush();
        } catch (IllegalStateException e) {
            // 【修改】首次创建时序抖动时，返回可自恢复页面，避免直接 500
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(
                    "<html><head><meta http-equiv='refresh' content='1'></head>"
                            + "<body><h3>支付单正在创建，请稍候自动重试...</h3></body></html>"
            );
            response.getWriter().flush();
        }
    }
}
