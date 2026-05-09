package com.xinjiema.hualimall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)  // 添加顺序，确保在其他组件之前执行
public class DatabaseSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        log.info("====== DatabaseSchemaInitializer 构造器已执行 ======");
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("====== 开始执行数据库表初始化 ======");

        try {
            ensureBuyerProfileTable();
            ensureMerchantProfileTable();
            ensureProductMerchantColumn();
            ensureProductMerchantIndex();
            ensurePaymentTable();
            log.info("====== 数据库表初始化完成 ======");
        } catch (Exception e) {
            log.error("数据库表初始化失败", e);
        }
    }

    private void ensurePaymentTable() {
        try {
            log.info("正在检查并创建 payment 表...");
            jdbcTemplate.execute("""
            -- 支付主表
                CREATE TABLE IF NOT EXISTS `payment` (
                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                  `payment_no` VARCHAR(64) NOT NULL,
                  `order_id` BIGINT NOT NULL,
                  `channel` VARCHAR(16) NOT NULL COMMENT 'ALIPAY|WECHAT',
                  `status` VARCHAR(16) NOT NULL COMMENT 'PENDING|PROCESSING|SUCCESS|CLOSED|FAILED',
                  `amount` DECIMAL(10,2) NOT NULL,
                  `pay_url` VARCHAR(512) DEFAULT NULL,
                  `code_url` VARCHAR(512) DEFAULT NULL,
                  `channel_trade_no` VARCHAR(128) DEFAULT NULL,
                  `expire_time` DATETIME DEFAULT NULL,
                  `paid_time` DATETIME DEFAULT NULL,
                  `close_time` DATETIME DEFAULT NULL,
                  `fail_reason` VARCHAR(255) DEFAULT NULL,
                  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (`id`),
                  UNIQUE KEY `uk_payment_no` (`payment_no`),
                  KEY `idx_order_id` (`order_id`),
                  KEY `idx_status` (`status`),
                  KEY `idx_channel_trade_no` (`channel_trade_no`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单';
            """);
            log.info("✓ payment 表创建或确认成功");
        } catch (Exception e) {
            log.error("✗ payment 表创建失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void ensureBuyerProfileTable() {
        try {
            log.info("正在检查并创建 buyer_profile 表...");
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS buyer_profile
                    (
                        id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                        user_id     BIGINT       NOT NULL UNIQUE,
                        real_name   VARCHAR(100) NOT NULL,
                        phone       VARCHAR(20)  NOT NULL,
                        create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """);
            log.info("✓ buyer_profile 表创建或确认成功");
        } catch (Exception e) {
            log.error("✗ buyer_profile 表创建失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void ensureMerchantProfileTable() {
        try {
            log.info("正在检查并创建 merchant_profile 表...");
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS merchant_profile
                    (
                        id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
                        user_id             BIGINT       NOT NULL UNIQUE,
                        shop_name           VARCHAR(200) NOT NULL,
                        business_license_no VARCHAR(100) NOT NULL,
                        contact_name        VARCHAR(100) NOT NULL,
                        contact_phone       VARCHAR(20)  NOT NULL,
                        create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """);
            log.info("✓ merchant_profile 表创建或确认成功");
        } catch (Exception e) {
            log.error("✗ merchant_profile 表创建失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void ensureProductMerchantColumn() {
        if (existsColumn("product", "merchant_id")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE product ADD COLUMN merchant_id BIGINT NULL AFTER category_id");
        log.info("已自动补齐 product.merchant_id 字段");
    }

    private void ensureProductMerchantIndex() {
        if (existsIndex("product", "idx_product_merchant_id")) {
            return;
        }
        jdbcTemplate.execute("CREATE INDEX idx_product_merchant_id ON product (merchant_id)");
        log.info("已自动创建索引 idx_product_merchant_id");
    }

    private boolean existsColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """, Integer.class, tableName, columnName);
        return count != null && count > 0;
    }

    private boolean existsIndex(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND index_name = ?
                """, Integer.class, tableName, indexName);
        return count != null && count > 0;
    }
}