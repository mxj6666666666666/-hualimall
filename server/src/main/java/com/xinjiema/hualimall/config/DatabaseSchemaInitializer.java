package com.xinjiema.hualimall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureBuyerProfileTable();
        ensureMerchantProfileTable();
        ensureProductMerchantColumn();
        ensureProductMerchantIndex();
    }

    private void ensureBuyerProfileTable() {
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
    }

    private void ensureMerchantProfileTable() {
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
