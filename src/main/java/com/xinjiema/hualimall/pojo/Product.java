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

    private String name;
    private Long categoryId;      // 不需要任何注解
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String description;
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
