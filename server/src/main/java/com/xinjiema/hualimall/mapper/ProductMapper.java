package com.xinjiema.hualimall.mapper;

import com.xinjiema.hualimall.pojo.ProQueryParams;
import com.xinjiema.hualimall.pojo.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product selectById(Long id);

    /**
     * 分页查询商品
     * @param proQueryParams 包含 offset, limit, categoryId, status, keyword 等参数
     */
    List<Product> selectProductPage(ProQueryParams proQueryParams);
    List<Product> selectProductPageByMerchant(@Param("merchantId") Long merchantId,
                                              @Param("categoryId") Long categoryId,
                                              @Param("status") Integer status,
                                              @Param("keyword") String keyword);


    void insertProduct(Product product);

    void updateProduct(Product product);

    void updateBatch(@Param("products") List<Product> products);

    void deleteProduct(Long id);

    void deleteBatch(@Param("ids") List<Long> ids);

    void insertBatch(@Param("products") List<Product> products);

    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    int updateProductByMerchant(@Param("product") Product product, @Param("merchantId") Long merchantId);

    int deleteProductByMerchant(@Param("id") Long id, @Param("merchantId") Long merchantId);


//    /**
//     * 统计符合条件的商品总数
//     * @param proQueryParams 查询条件参数
//     */
//    long countProduct(ProQueryParams proQueryParams);
}
