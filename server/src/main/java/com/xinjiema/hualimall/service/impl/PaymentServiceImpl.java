package com.xinjiema.hualimall.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.xinjiema.hualimall.config.AlipayConfig;
import com.xinjiema.hualimall.config.WechatPayProperties;
import com.xinjiema.hualimall.mapper.OrderMapper;
import com.xinjiema.hualimall.mapper.PaymentMapper;
import com.xinjiema.hualimall.pojo.CreatePaymentRequest;
import com.xinjiema.hualimall.pojo.Order;
import com.xinjiema.hualimall.pojo.Payment;
import com.xinjiema.hualimall.service.PaymentService;
import com.xinjiema.hualimall.utils.PaymentNoGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String WECHAT_SUCCESS_XML = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    private static final String WECHAT_FAIL_XML = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[FAIL]]></return_msg></xml>";

    private final AlipayConfig alipayConfig;
    private final WechatPayProperties wechatPayProperties;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final PaymentNoGenerator paymentNoGenerator;

    public PaymentServiceImpl(AlipayConfig alipayConfig,
                              WechatPayProperties wechatPayProperties,
                              PaymentMapper paymentMapper,
                              OrderMapper orderMapper,
                              PaymentNoGenerator paymentNoGenerator) {
        this.alipayConfig = alipayConfig;
        this.wechatPayProperties = wechatPayProperties;
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.paymentNoGenerator = paymentNoGenerator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment createPayment(CreatePaymentRequest req, Long currentUserId, String currentRole) {
        if (req == null || req.getOrderId() == null || req.getChannel() == null) {
            throw new IllegalArgumentException("订单ID和支付渠道不能为空");
        }
        requireBuyerRole(currentRole);
        String channel = req.getChannel().trim().toUpperCase(Locale.ROOT);

        Order order = orderMapper.selectByIdAndUserId(req.getOrderId(), currentUserId);
        if (order == null) {
            throw new SecurityException("订单不存在或无支付权限");
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new IllegalStateException("订单状态不允许支付，当前状态: " + order.getStatus());
        }

        Payment existing = paymentMapper.selectActiveByOrderId(order.getId());
        if (existing != null) {
            return existing;
        }

        Payment payment = new Payment();
        payment.setPaymentNo(paymentNoGenerator.generate());
        payment.setOrderId(order.getId());
        payment.setChannel(channel);
        payment.setStatus("PENDING");
        payment.setAmount(order.getTotalAmount());
        payment.setCreateTime(OffsetDateTime.now());
        payment.setUpdateTime(OffsetDateTime.now());
        payment.setExpireTime(OffsetDateTime.now().plusMinutes(15));

        if ("ALIPAY".equals(channel)) {
            payment.setPayUrl(buildAlipayEntryUrl(payment.getPaymentNo()));
        } else if ("WECHAT".equals(channel)) {
            String codeUrl = "weixin://wxpay/bizpayurl?pr=" + payment.getPaymentNo();
            payment.setCodeUrl(codeUrl);
            payment.setPayUrl(codeUrl);
        } else {
            throw new IllegalArgumentException("不支持的支付渠道: " + channel);
        }

        paymentMapper.insert(payment);
        return payment;
    }

    @Override
    public String buildAlipayForm(String paymentNo, Long currentUserId, String currentRole) throws AlipayApiException {
        requireBuyerOrAdminRole(currentRole);
        Payment payment = paymentMapper.selectByPaymentNo(paymentNo);
        if (payment == null) {
            throw new IllegalArgumentException("支付单不存在");
        }
        assertOrderAccessible(payment.getOrderId(), currentUserId, currentRole);
        if (!"ALIPAY".equals(payment.getChannel())) {
            throw new IllegalStateException("该支付单不是支付宝渠道");
        }
        Order order = orderMapper.selectById(payment.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return generateAlipayForm(order, paymentNo);
    }

    @Override
    public Payment getById(Long id, Long currentUserId, String currentRole) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new IllegalArgumentException("支付单不存在: " + id);
        }
        assertOrderAccessible(payment.getOrderId(), currentUserId, currentRole);
        return payment;
    }

    @Override
    public Payment getLatestByOrderId(Long orderId, Long currentUserId, String currentRole) {
        assertOrderAccessible(orderId, currentUserId, currentRole);
        Payment payment = paymentMapper.selectLatestByOrderId(orderId);
        if (payment == null) {
            throw new IllegalArgumentException("该订单暂无支付记录: " + orderId);
        }
        return payment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePayment(Long id, Long currentUserId, String currentRole) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new IllegalArgumentException("支付单不存在: " + id);
        }
        assertOrderAccessible(payment.getOrderId(), currentUserId, currentRole);
        if (!"PENDING".equals(payment.getStatus()) && !"PROCESSING".equals(payment.getStatus())) {
            throw new IllegalStateException("当前状态不允许关闭: " + payment.getStatus());
        }
        int rows = paymentMapper.updateStatusToClosed(id, OffsetDateTime.now());
        if (rows == 0) {
            throw new IllegalStateException("关闭失败，支付单状态可能已变更");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(HttpServletRequest request) throws AlipayApiException {
        Map<String, String> params = collectRequestParams(request);
        boolean signatureValid = AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType()
        );
        if (!signatureValid) {
            log.warn("支付宝回调验签失败");
            return "failure";
        }

        String appId = params.get("app_id");
        if (appId == null || !appId.equals(alipayConfig.getAppId())) {
            log.warn("支付宝回调 app_id 非法: {}", appId);
            return "failure";
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");
        String totalAmountStr = params.get("total_amount");
        if (outTradeNo == null || tradeStatus == null || tradeNo == null || totalAmountStr == null) {
            return "failure";
        }

        Payment payment = paymentMapper.selectByPaymentNo(outTradeNo);
        if (payment == null) {
            return "failure";
        }
        if ("SUCCESS".equals(payment.getStatus())) {
            return "success";
        }

        BigDecimal notifyAmount;
        try {
            notifyAmount = new BigDecimal(totalAmountStr);
        } catch (NumberFormatException e) {
            return "failure";
        }
        if (payment.getAmount().compareTo(notifyAmount) != 0) {
            return "failure";
        }

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            int rows = paymentMapper.updateStatusToSuccess(payment.getId(), tradeNo, OffsetDateTime.now());
            if (rows > 0) {
                orderMapper.updateOrderStatus(payment.getOrderId(), 1);
            }
            return "success";
        }
        if ("TRADE_CLOSED".equals(tradeStatus)) {
            paymentMapper.updateStatusToClosed(payment.getId(), OffsetDateTime.now());
            return "success";
        }
        return "failure";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleWechatNotify(String notifyBody) {
        try {
            Map<String, String> params = parseXmlToMap(notifyBody);
            if (!"SUCCESS".equals(params.get("return_code")) || !"SUCCESS".equals(params.get("result_code"))) {
                return WECHAT_FAIL_XML;
            }
            if (!isWechatSignatureValid(params, wechatPayProperties.getApiV2Key())) {
                return WECHAT_FAIL_XML;
            }

            String outTradeNo = params.get("out_trade_no");
            String transactionId = params.get("transaction_id");
            String totalFee = params.get("total_fee");
            if (outTradeNo == null || transactionId == null || totalFee == null) {
                return WECHAT_FAIL_XML;
            }

            Payment payment = paymentMapper.selectByPaymentNo(outTradeNo);
            if (payment == null) {
                return WECHAT_FAIL_XML;
            }
            if ("SUCCESS".equals(payment.getStatus())) {
                return WECHAT_SUCCESS_XML;
            }

            BigDecimal callbackAmount = new BigDecimal(totalFee).movePointLeft(2);
            if (payment.getAmount().compareTo(callbackAmount) != 0) {
                return WECHAT_FAIL_XML;
            }

            int rows = paymentMapper.updateStatusToSuccess(payment.getId(), transactionId, OffsetDateTime.now());
            if (rows > 0) {
                orderMapper.updateOrderStatus(payment.getOrderId(), 1);
            }
            return WECHAT_SUCCESS_XML;
        } catch (Exception e) {
            log.warn("微信回调处理失败: {}", e.getMessage());
            return WECHAT_FAIL_XML;
        }
    }

    @Override
    public void reconcileTimeoutPayments() {
        List<Payment> list = paymentMapper.selectTimeoutPending(OffsetDateTime.now());
        for (Payment p : list) {
            try {
                int rows = paymentMapper.updateStatusToClosed(p.getId(), OffsetDateTime.now());
                if (rows > 0) {
                    log.info("超时支付单已自动关闭: {}", p.getPaymentNo());
                }
            } catch (Exception e) {
                log.warn("自动关单失败: {}", p.getPaymentNo());
            }
        }
    }

    private void assertOrderAccessible(Long orderId, Long currentUserId, String currentRole) {
        if ("ADMIN".equals(currentRole)) {
            return;
        }
        Order order = orderMapper.selectByIdAndUserId(orderId, currentUserId);
        if (order == null) {
            throw new SecurityException("订单不存在或无访问权限");
        }
    }

    private void requireBuyerRole(String currentRole) {
        if (!"BUYER".equals(currentRole)) {
            throw new SecurityException("仅买家可发起支付");
        }
    }

    private void requireBuyerOrAdminRole(String currentRole) {
        if (!"BUYER".equals(currentRole) && !"ADMIN".equals(currentRole)) {
            throw new SecurityException("无支付权限");
        }
    }

    private String buildAlipayEntryUrl(String paymentNo) {
        return "/pay/alipay/" + paymentNo;
    }

    private String generateAlipayForm(Order order, String outTradeNo) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getAppPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
        );
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
        alipayRequest.setReturnUrl(alipayConfig.getReturnUrl());

        String subject = "花礼商城";
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            if (order.getItems().size() == 1) {
                subject = order.getItems().get(0).getProductName();
            } else {
                subject = order.getItems().get(0).getProductName() + " 等" + order.getItems().size() + "件商品";
            }
        }
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", order.getTotalAmount().toString());
        bizContent.put("subject", subject);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        alipayRequest.setBizContent(bizContent.toString());
        return alipayClient.pageExecute(alipayRequest).getBody();
    }

    private Map<String, String> collectRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                continue;
            }
            params.put(entry.getKey(), String.join(",", values));
        }
        return params;
    }

    private Map<String, String> parseXmlToMap(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setExpandEntityReferences(false);
        Document document = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        NodeList nodeList = document.getDocumentElement().getChildNodes();
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                map.put(node.getNodeName(), node.getTextContent());
            }
        }
        return map;
    }

    private boolean isWechatSignatureValid(Map<String, String> params, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未配置微信支付 APIv2 密钥");
        }
        String sign = params.get("sign");
        if (sign == null || sign.isBlank()) {
            return false;
        }
        SortedMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ("sign".equals(key) || value == null || value.isBlank()) {
                continue;
            }
            sorted.put(key, value);
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("key=").append(apiKey);
        String localSign = md5Hex(sb.toString()).toUpperCase(Locale.ROOT);
        return localSign.equals(sign.toUpperCase(Locale.ROOT));
    }

    private String md5Hex(String content) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("微信签名计算失败", e);
        }
    }
}

