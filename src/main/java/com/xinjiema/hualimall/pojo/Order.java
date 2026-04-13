package com.xinjiema.hualimall.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private String orderNo;       // 订单编号
    private Long userId;          // 用户ID
    private BigDecimal totalAmount; // 总金额
    private Integer status;       // 订单状态：0-待支付 1-已支付 2-已取消 3-已完成
    
    // 数据库自动设置
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段，用于保存订单的商品明细
    private List<OrderItem> items;
}

/*
 ======================= 数据库相关的建表语句 =======================
 CREATE TABLE `orders` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   `order_no` varchar(64) NOT NULL COMMENT '订单编号',
   `user_id` bigint DEFAULT NULL COMMENT '用户ID',
   `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总金额',
   `status` int NOT NULL DEFAULT '0' COMMENT '订单状态：0-待支付 1-已支付 2-已取消 3-已完成',
   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   PRIMARY KEY (`id`),
   UNIQUE KEY `uk_order_no` (`order_no`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
 */

