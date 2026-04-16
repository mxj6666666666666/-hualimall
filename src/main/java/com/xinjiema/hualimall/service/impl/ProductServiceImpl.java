package com.xinjiema.hualimall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xinjiema.hualimall.mapper.ProductMapper;
import com.xinjiema.hualimall.pojo.PageResult;
import com.xinjiema.hualimall.pojo.ProQueryParams;
import com.xinjiema.hualimall.pojo.Product;
import com.xinjiema.hualimall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductMapper productMapper;

    @Override
    public Product findById(Long id) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("商品ID非法");
        }
        return productMapper.selectById(id);
    }

    @Override
    public PageResult<Product> findall(ProQueryParams proQueryParams) {
        if (proQueryParams.getPage() == null || proQueryParams.getPage() < 1) {
            throw new IllegalArgumentException("page 必须大于等于 1");
        }
        if (proQueryParams.getPageSize() == null || proQueryParams.getPageSize() < 1) {
            throw new IllegalArgumentException("pageSize 必须大于等于 1");
        }
        PageHelper.startPage(proQueryParams.getPage(), proQueryParams.getPageSize());
        List<Product> list = productMapper.selectProductPage(proQueryParams);
        Page<Product> productPage = (Page<Product>) list;
        return new PageResult<Product>(productPage.getTotal(), productPage.getResult());
    }

    @Override
    public void addProduct(Product product) {
        productMapper.insertProduct(product);
    }

    @Override
    public void addBatch(List<Product> products) {
        productMapper.insertBatch(products);
    }

    @Override
    public void updateProduct(Product product) {
        productMapper.updateProduct(product);
    }

    @Override
    public void updateBatch(List<Product> products) {
        productMapper.updateBatch(products);
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteProduct(id);
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        productMapper.deleteBatch(ids);
    }
}
