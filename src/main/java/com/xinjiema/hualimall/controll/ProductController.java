package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.ProQueryParams;
import com.xinjiema.hualimall.pojo.Product;
import com.xinjiema.hualimall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public List<Product> list(ProQueryParams proQueryParams) {
        productService.findall(proQueryParams);
    }
}
