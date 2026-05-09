package com.xinjiema.hualimall.task;

import com.xinjiema.hualimall.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling  // 启用定时任务
public class PaymentReconcileTask {

    @Autowired
    private PaymentService paymentService;

    /**
     * 每30秒检查一次超时支付单（可改为30分钟或更长时间）
     * cron表达式：秒 分 时 日 月 星期
     */
    @Scheduled(cron = "0 */5 * * * ?")  // 每5分钟执行一次（生产环境推荐）
    // @Scheduled(fixedDelay = 30000)    // 开发测试用：每30秒一次
    public void checkTimeoutPayments() {
        log.info("开始检查超时支付单...");
        try {
            paymentService.reconcileTimeoutPayments();
        } catch (Exception e) {
            log.error("检查超时支付单异常", e);
        }
    }
}