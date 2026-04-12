package com.xinjiema.hualimall.mapper;

import com.xinjiema.hualimall.pojo.ProQueryParams;
import com.xinjiema.hualimall.pojo.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    /**
     * 分页查询商品
     * @param proQueryParams 包含 offset, limit, categoryId, status, keyword 等参数
     */
    List<Product> selectProductPage(ProQueryParams proQueryParams);


    void insertProduct(Product product);

    void updateProduct(Product product);

    void updateBatch(@Param("products") List<Product> products);

    void deleteProduct(Long id);

    void deleteBatch(List<Long> ids);

    void insertBatch(List<Product> products);

//    /**
//     * 统计符合条件的商品总数
//     * @param proQueryParams 查询条件参数
//     */
//    long countProduct(ProQueryParams proQueryParams);
}