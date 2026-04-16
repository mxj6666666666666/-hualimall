package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.PageResult;
import com.xinjiema.hualimall.pojo.ProQueryParams;
import com.xinjiema.hualimall.pojo.Product;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public Result<Product> findById(@PathVariable Long id) {
        log.info("查询 id = {} 的商品", id);
        Product product = productService.findById(id);
        return Result.success(product);
    }

    @GetMapping({"", "/list"})
    public Result<PageResult<Product>> list(ProQueryParams proQueryParams) {
        log.info("查询商品列表，参数：{}", proQueryParams);
        PageResult<Product> result = productService.findall(proQueryParams);
        log.info("查询商品列表结果：{}", result);
        return  Result.success(result);
    }

}
