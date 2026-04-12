package com.xinjiema.hualimall.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Long id;

    private String name;    // not null
    private Long categoryId;      // 不需要任何注解
    private BigDecimal price;   // not null
    private Integer stock;  // not null (default 0)
    private String imageUrl;
    private String description;
    private Integer status;

    //数据库自动设置
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
