package com.xinjiema.hualimall.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.xinjiema.hualimall.config.AlipayConfig;
import com.xinjiema.hualimall.mapper.OrderMapper;      // 假设你已有订单 Mapper
import com.xinjiema.hualimall.mapper.PaymentMapper;
import com.xinjiema.hualimall.pojo.CreatePaymentRequest;
import com.xinjiema.hualimall.pojo.Order;              // 假设你已有订单实体
import com.xinjiema.hualimall.pojo.Payment;
import com.xinjiema.hualimall.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private AlipayConfig alipayConfig;


    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderMapper orderMapper;


    // ======================== 1. 创建支付单 ========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment createPayment(CreatePaymentRequest req, HttpServletResponse httpResponse) throws IOException {
        if (req.getOrderId() == null || req.getChannel() == null) {
            throw new IllegalArgumentException("订单ID和支付渠道不能为空");
        }

        // ① 幂等：若已存在未终态的支付单，直接返回
        Payment exist = paymentMapper.selectActiveByOrderId(req.getOrderId());
        if (exist != null) {
            log.info("订单 {} 已有进行中的支付单 {}，直接返回", req.getOrderId(), exist.getPaymentNo());
            return exist;
        }

        // ② 检查订单是否存在且可支付 (订单状态=0 待支付)
        Order order = orderMapper.selectById(req.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + req.getOrderId());
        }
        if (order.getStatus() != 0) {
            throw new IllegalStateException("订单状态不允许支付，当前状态: " + order.getStatus());
        }

        // ③ 组装支付单
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(req.getOrderId());
        payment.setChannel(req.getChannel().toUpperCase());
        payment.setStatus("PENDING");
        payment.setAmount(order.getTotalAmount());          // 金额以订单为准
        OffsetDateTime now = OffsetDateTime.now();
        payment.setCreateTime(now);
        payment.setUpdateTime(now);
        payment.setExpireTime(now.plusMinutes(15));        // 15分钟过期

        // ============ TODO 重要：请替换为你的真实支付宝/微信 SDK 调用 ============
        try {
            if ("ALIPAY".equals(payment.getChannel())) {
                // ---- 支付宝沙箱示例（电脑网站/扫码支付） ----
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
                 AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

                // ③ 创建支付请求
                AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
                alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());   // 异步回调
                alipayRequest.setReturnUrl(alipayConfig.getReturnUrl());   // 同步跳转

                // ⑤ 业务参数——全部从 order 里拿
                String subject = "花礼商城";
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    if (order.getItems().size() == 1) {
                        subject = order.getItems().getFirst().getProductName();
                    } else {
                        subject = order.getItems().getFirst().getProductName()
                                + " 等" + order.getItems().size() + "件商品";
                    }
                }
                JSONObject bizContent = new JSONObject();
                bizContent.put("out_trade_no", order.getOrderNo());         // 订单号
                bizContent.put("total_amount", order.getTotalAmount().toString()); // 金额
                bizContent.put("subject", subject); //标题
                bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
                alipayRequest.setBizContent(bizContent.toString());

                // ④ 执行请求，获取支付宝返回的表单HTML
                String formHtml = alipayClient.pageExecute(request).getBody();
                // ⑤ 将HTML表单直接输出给浏览器，浏览器会自动跳转到支付宝收银台
                httpResponse.setContentType("text/html;charset=" + alipayConfig.getCharset());
                httpResponse.getWriter().write(formHtml);
                httpResponse.getWriter().flush();
                httpResponse.getWriter().close();
                // request.setBizContent(构建JSON，包含 out_trade_no, total_amount, subject 等);
                // AlipayTradePrecreateResponse response = client.execute(request);
            } else if ("WECHAT".equals(payment.getChannel())) {
                // ---- 微信沙箱示例（扫码支付） ----
                // 类似调用微信 SDK，生成 code_url
                // -------------------------------------------------
                String mockUrl = "weixin://wxpay/bizpayurl?pr=mock_" + payment.getPaymentNo();
                payment.setPayUrl(mockUrl);
                payment.setCodeUrl(mockUrl);
            } else {
                throw new IllegalArgumentException("不支持的支付渠道: " + payment.getChannel());
            }
        } catch (Exception e) {
            log.error("调用支付渠道创建订单失败", e);
            throw new RuntimeException("创建支付订单失败，请稍后重试", e);
        }

        // ⑤ 入库
        paymentMapper.insert(payment);
        log.info("创建支付单成功: {}", payment.getPaymentNo());
        return payment;
    }

    // ======================== 2. 查询支付单详情 ========================
    @Override
    public Payment getById(Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new IllegalArgumentException("支付单不存在: " + id);
        }
        return payment;
    }

    // ======================== 3. 根据订单查询最新支付单 ========================
    @Override
    public Payment getLatestByOrderId(Long orderId) {
        Payment payment = paymentMapper.selectLatestByOrderId(orderId);
        if (payment == null) {
            throw new IllegalArgumentException("该订单暂无支付记录: " + orderId);
        }
        return payment;
    }

    // ======================== 4. 关闭支付单 ========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePayment(Long id) {
        Payment payment = getById(id);
        String status = payment.getStatus();

        // 仅 PENDING / PROCESSING 可关闭
        if (!"PENDING".equals(status) && !"PROCESSING".equals(status)) {
            if ("SUCCESS".equals(status)) {
                throw new IllegalStateException("支付已完成，无法关闭");
            }
            throw new IllegalStateException("当前状态不允许关闭: " + status);
        }

        // 若有渠道交易号（已扫码但未付款），尝试通知渠道关单
        if (payment.getChannelTradeNo() != null) {
            closeChannelOrder(payment);
        }

        // 更新本地状态为 CLOSED
        OffsetDateTime now = OffsetDateTime.now();
        int rows = paymentMapper.updateStatusToClosed(id, now);
        if (rows == 0) {
            throw new IllegalStateException("关闭失败，支付单状态可能已变更");
        }
        log.info("支付单 {} 已关闭", payment.getPaymentNo());
    }

    // ======================== 5. 支付宝异步通知处理 ========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params) throws AlipayApiException {
        log.info("收到支付宝回调: {}", params);

        // ① 验签（必须做，否则有安全风险）
        // ============ TODO 替换为你的支付宝验签实现 ============
        String sign = params.get("sign");
        String content = AlipaySignature.getSignCheckContentV1(params);
        boolean checkSignature = AlipaySignature.rsa256CheckContent(
                content, sign, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset()
        );

        // ② 提取参数
        String outTradeNo = params.get("out_trade_no");        // 你的订单编号
        String tradeNo = params.get("trade_no");               // 支付宝交易号
        String totalAmountStr = params.get("total_amount");       // 交易金额
        String buyerId = params.get("buyer_id");               // 买家支付宝用户ID
        String gmtPayment = params.get("gmt_payment");         // 交易付款时间
        String tradeStatus = params.get("trade_status");

        if (outTradeNo == null || tradeStatus == null) {
            log.error("回调参数缺失");
            return "failure";
        }

        // ③ 查询本地支付单
        Payment payment = paymentMapper.selectByPaymentNo(outTradeNo);
        if (payment == null) {
            log.error("本地支付单不存在: {}", outTradeNo);
            return "failure";
        }

        // ④ 幂等：已成功则直接返回
        if ("SUCCESS".equals(payment.getStatus())) {
            return "success";
        }

        // ⑤ 金额校验
        try {
            BigDecimal notifyAmount = new BigDecimal(totalAmountStr);
            if (payment.getAmount().compareTo(notifyAmount) != 0) {
                log.error("金额不一致: 本地={}, 回调={}", payment.getAmount(), notifyAmount);
                return "failure";
            }
        } catch (NumberFormatException e) {
            log.error("回调金额格式错误: {}", totalAmountStr);
            return "failure";
        }

        // ⑥ 根据交易状态更新本地
        if (checkSignature) {
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功
                OffsetDateTime paidTime = OffsetDateTime.now();
                int rows = paymentMapper.updateStatusToSuccess(payment.getId(), tradeNo, paidTime);
                if (rows > 0) {
                    // 联动更新订单状态为“已支付”(1)
                    orderMapper.updateOrderStatus(payment.getOrderId(), 1);
                    log.info("订单编号: {}", outTradeNo);
                    log.info("支付宝交易号: {}", tradeNo);
                    log.info("交易金额: {}", totalAmountStr);
                    log.info("买家ID: {}", buyerId);
                    log.info("付款时间: {}", gmtPayment);
                }
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                // 交易关闭（超时等原因）
                OffsetDateTime closeTime = OffsetDateTime.now();
                paymentMapper.updateStatusToClosed(payment.getId(), closeTime);
                log.info("支付单关闭(来自回调): {}", outTradeNo);
            }
        }else {
            System.out.println("验签失败！可能是伪造的回调请求");
            return "fail";
        }
        return "success";
    }

    // ======================== 6. 微信异步通知处理 ========================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleWechatNotify(String notifyBody) {
        log.info("收到微信回调: {}", notifyBody);
        // TODO 实现微信回调：解析XML -> 验签 -> 金额校验 -> 更新支付单+订单状态 -> 返回SUCCESS或FAIL XML
        // 以下为伪代码，请自行填充
        /*
        Map<String, String> params = WXPayUtil.xmlToMap(notifyBody);
        if (!WXPayUtil.isSignatureValid(params, apiKey)) return failXml();
        String outTradeNo = params.get("out_trade_no");
        Payment payment = paymentMapper.selectByPaymentNo(outTradeNo);
        ... 类似支付宝流程
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
        */
        log.warn("微信回调尚未完整实现");
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
    }

    // ======================== 7. 超时支付单对账/关单 ========================
    @Override
    public void reconcileTimeoutPayments() {
        List<Payment> list = paymentMapper.selectTimeoutPending(OffsetDateTime.now());
        for (Payment p : list) {
            try {
                if (p.getChannelTradeNo() != null) {
                    closeChannelOrder(p);
                }
                int rows = paymentMapper.updateStatusToClosed(p.getId(), OffsetDateTime.now());
                if (rows > 0) {
                    log.info("超时支付单已自动关闭: {}", p.getPaymentNo());
                }
            } catch (Exception e) {
                log.error("自动关单失败: {}, 原因: {}", p.getPaymentNo(), e.getMessage());
            }
        }
    }

    // ======================== 私有辅助方法 ========================
    private String generatePaymentNo() {
        String timePart = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = new Random().nextInt(10000);
        return String.format("P%s%04d", timePart, rand);
    }

    private void closeChannelOrder(Payment payment) {
        // TODO 实现真实的支付宝/微信关单逻辑
        if ("ALIPAY".equals(payment.getChannel())) {
            // AlipayClient client = ...;
            // AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            // request.setBizContent("{\"out_trade_no\":\"" + payment.getPaymentNo() + "\"}");
            // client.execute(request);
            log.info("调用支付宝关单: {}", payment.getPaymentNo());
        } else if ("WECHAT".equals(payment.getChannel())) {
            // 微信关单API
            log.info("调用微信关单: {}", payment.getPaymentNo());
        }
    }
}