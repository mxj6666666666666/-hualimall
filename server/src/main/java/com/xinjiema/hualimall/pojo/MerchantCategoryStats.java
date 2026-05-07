package com.xinjiema.hualimall.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCategoryStats {
    private Long categoryId;
    private BigDecimal salesAmount;
    private Long orderCount;
}
