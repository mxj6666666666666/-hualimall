package com.xinjiema.hualimall.controll;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.xinjiema.hualimall.config.AlipayConfig;
import com.xinjiema.hualimall.pojo.AliPayOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alipay")
public class AlipayController {

    @Autowired
    private AlipayConfig alipayConfig;

    // 如果你的项目有订单Mapper，可以注入并在这里使用
    // @Resource
    // private OrderMapper orderMapper;

    // ============ 1. 发起支付接口 ============
    @GetMapping("/pay")
    public void pay(AliPayOrder aliPayOrder, HttpServletResponse httpResponse) throws Exception {

        // ① 创建支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),       // 网关地址
                alipayConfig.getAppId(),            // APPID
                alipayConfig.getAppPrivateKey(),    // 应用私钥
                alipayConfig.getFormat(),           // 格式：JSON
                alipayConfig.getCharset(),          // 字符集：UTF-8
                alipayConfig.getAlipayPublicKey(),  // 支付宝公钥
                alipayConfig.getSignType()          // 签名方式：RSA2
        );

        // ② 创建支付请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 设置异步回调地址（付款成功后支付宝会请求这个地址）
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        // 设置同步跳转地址（支付完成后用户浏览器跳转的地址）
        request.setReturnUrl(alipayConfig.getReturnUrl());

        // ③ 设置业务参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", aliPayOrder.getTraceNo());      // 你的订单编号
        bizContent.put("total_amount", aliPayOrder.getTotalAmount());  // 订单金额（元）
        bizContent.put("subject", aliPayOrder.getSubject());            // 订单标题
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");      // 固定值：电脑网站支付
        request.setBizContent(bizContent.toString());

        // ④ 执行请求，获取支付宝返回的表单HTML
        String formHtml = alipayClient.pageExecute(request).getBody();

        // ⑤ 将HTML表单直接输出给浏览器，浏览器会自动跳转到支付宝收银台
        httpResponse.setContentType("text/html;charset=" + alipayConfig.getCharset());
        httpResponse.getWriter().write(formHtml);
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }


    // ============ 2. 异步回调接口（必须是POST）============
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) throws Exception {

        // ① 获取支付宝回调的所有参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        // ② 验证签名（确保回调确实是支付宝发来的）
        String sign = params.get("sign");
        String content = AlipaySignature.getSignCheckContentV1(params);
        boolean checkSignature = AlipaySignature.rsa256CheckContent(
                content, sign, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset()
        );

        if (checkSignature) {
            // 验签通过
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功！
                String outTradeNo = params.get("out_trade_no");        // 你的订单编号
                String tradeNo = params.get("trade_no");               // 支付宝交易号
                String totalAmount = params.get("total_amount");       // 交易金额
                String buyerId = params.get("buyer_id");               // 买家支付宝用户ID
                String gmtPayment = params.get("gmt_payment");         // 交易付款时间

                System.out.println("======== 支付成功 ========");
                System.out.println("订单编号: " + outTradeNo);
                System.out.println("支付宝交易号: " + tradeNo);
                System.out.println("交易金额: " + totalAmount);
                System.out.println("买家ID: " + buyerId);
                System.out.println("付款时间: " + gmtPayment);

                // TODO: 在这里更新你的订单状态为「已支付」
                // orderMapper.updateStatus(outTradeNo, "已支付", tradeNo, gmtPayment);
            }
        } else {
            System.out.println("验签失败！可能是伪造的回调请求");
            return "fail";
        }

        // ③ 必须返回 "success"，否则支付宝会持续重试回调
        return "success";
    }
}