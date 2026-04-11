package com.xinjiema.hualimall.mapper;

import com.xinjiema.hualimall.pojo.Product;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {

    /**
     * 分页查询商品
     * @param params 包含 offset, limit, categoryId, status, keyword 等参数
     */
    List<Product> selectProductPage(Map<String, Object> params);

    /**
     * 统计符合条件的商品总数
     * @param params 查询条件参数
     */
    int countProduct(Map<String, Object> params);
}