package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.Product;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.service.ProductService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/products")
public class SuProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public Result<String> addProduct(Product product) {

        log.info("添加商品，参数：{}", product);
        productService.addProduct(product);

        return Result.success("添加商品成功");
    }

}
