package com.xinjiema.hualimall.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private Long id;
    private Long orderId;         // 订单ID
    private Long productId;       // 商品ID
    private String productName;   // 商品名称
    private BigDecimal productPrice; // 商品单价
    private Integer quantity;     // 购买数量
    private BigDecimal totalPrice;// 该商品总价
}

/*
 ======================= 数据库相关的建表语句 =======================
 CREATE TABLE `order_item` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   `order_id` bigint NOT NULL COMMENT '订单ID',
   `product_id` bigint NOT NULL COMMENT '商品ID',
   `product_name` varchar(128) NOT NULL COMMENT '商品名称',
   `product_price` decimal(10,2) NOT NULL COMMENT '商品单价',
   `quantity` int NOT NULL COMMENT '购买数量',
   `total_price` decimal(10,2) NOT NULL COMMENT '该商品总价',
   PRIMARY KEY (`id`),
   KEY `idx_order_id` (`order_id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
 */

