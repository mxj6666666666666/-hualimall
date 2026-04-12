package com.xinjiema.hualimall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xinjiema.hualimall.mapper.ProductMapper;
import com.xinjiema.hualimall.pojo.PageResult;
import com.xinjiema.hualimall.pojo.ProQueryParams;
import com.xinjiema.hualimall.pojo.Product;
import com.xinjiema.hualimall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductMapper productMapper;

    @Override
    public PageResult<Product> findall(ProQueryParams proQueryParams) {
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
