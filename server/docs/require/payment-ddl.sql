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

-- 回调日志表（可选但推荐）
CREATE TABLE IF NOT EXISTS `payment_notify_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_id` BIGINT DEFAULT NULL,
  `channel` VARCHAR(16) NOT NULL,
  `notify_body` LONGTEXT NOT NULL,
  `verify_result` VARCHAR(32) NOT NULL,
  `process_result` VARCHAR(32) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_payment_id` (`payment_id`),
  KEY `idx_channel` (`channel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付回调日志';

-- 创建支付幂等优化（MySQL 8+ 支持函数索引时可选方案）
-- 核心目的：同一订单同一时间最多一条进行中支付单
-- 建议由业务层先查 selectActiveByOrderId(orderId) 做兜底幂等。
