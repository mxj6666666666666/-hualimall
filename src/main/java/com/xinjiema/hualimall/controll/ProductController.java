package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.ProQueryParam;
import com.xinjiema.hualimall.pojo.Product;
import com.xinjiema.hualimall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {
    @GetMapping("/list")
    public List<Product> list(ProQueryParam  proQueryParam) {
         ProductService.findall(proQueryParam.getPage(),proQueryParam.getSize());
    }
}
