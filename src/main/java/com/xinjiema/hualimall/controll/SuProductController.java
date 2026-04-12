package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.Product;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.service.ProductService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/products")
public class SuProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public Result<String> addProduct(@RequestBody Product product) {
        log.info("添加商品，参数：{}", product);
        productService.addProduct(product);
        return Result.success("添加商品成功");
    }

    @PostMapping("/batch")
    public Result<String> addBatch(@RequestBody List<Product> products) {
        log.info("批量添加商品，参数：{}", products);
        productService.addBatch(products);
        return Result.success("批量添加商品成功");
    }

    @PutMapping("/{id}")
    public Result<String> updateProduct(@PathVariable Long id, Product product) {
        log.info("修改 id = {} 的商品，参数：{}", id, product);
        product.setId(id);
        productService.updateProduct(product);
        return Result.success("修改商品成功");
    }

    @PutMapping("/batch")
    public Result<String> updateBatch(@RequestBody List<Product> products) {
        log.info("批量修改商品，参数：{}", products);
        productService.updateBatch(products);
        return Result.success("批量修改商品成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        log.info("删除 id = {} 的商品", id);
        productService.deleteProduct(id);
        return Result.success("删除商品成功");
    }

    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        log.info("批量删除商品，参数：{}", ids);
        productService.deleteBatch(ids);
        return Result.success("批量删除商品成功");
    }
}
