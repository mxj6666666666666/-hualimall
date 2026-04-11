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
        Page<Product> productPage = PageHelper.startPage(proQueryParams.getPage(), proQueryParams.getPageSize());
        productMapper.selectProductPage(proQueryParams);
        return new PageResult<Product>(productPage.getTotal(), productPage.getResult());
    }
}
