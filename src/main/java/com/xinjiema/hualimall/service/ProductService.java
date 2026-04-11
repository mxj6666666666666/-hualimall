package com.xinjiema.hualimall.service;

import com.xinjiema.hualimall.pojo.PageResult;
import com.xinjiema.hualimall.pojo.Product;

public interface ProductService {
    PageResult<Product> findall(Integer page, Integer size);
}
