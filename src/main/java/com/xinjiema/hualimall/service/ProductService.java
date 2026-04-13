package com.xinjiema.hualimall.service;

import com.xinjiema.hualimall.pojo.PageResult;
import com.xinjiema.hualimall.pojo.ProQueryParams;
import com.xinjiema.hualimall.pojo.Product;

import java.util.List;

public interface ProductService {

    Product findById(Long id);

    PageResult<Product> findall(ProQueryParams proQueryParams);

    void addProduct(Product product);

    void updateProduct(Product product);

    void updateBatch(List<Product> product);

    void deleteProduct(Long id);

    void deleteBatch(List<Long> ids);

    void addBatch(List<Product> products);

}
