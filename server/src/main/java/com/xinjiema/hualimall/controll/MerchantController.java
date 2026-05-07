package com.xinjiema.hualimall.controll;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xinjiema.hualimall.mapper.OrderMapper;
import com.xinjiema.hualimall.mapper.ProductMapper;
import com.xinjiema.hualimall.pojo.*;
import com.xinjiema.hualimall.utils.AuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    public MerchantController(ProductMapper productMapper, OrderMapper orderMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
    }

    @GetMapping("/products")
    public Result<PageResult<Product>> listProducts(ProQueryParams queryParams) {
        if (queryParams.getPage() == null || queryParams.getPage() < 1) {
            throw new IllegalArgumentException("page 必须大于等于 1");
        }
        if (queryParams.getPageSize() == null || queryParams.getPageSize() < 1) {
            throw new IllegalArgumentException("pageSize 必须大于等于 1");
        }
        Long merchantId = AuthContext.requireCurrentUserId();
        PageHelper.startPage(queryParams.getPage(), queryParams.getPageSize());
        List<Product> products = productMapper.selectProductPageByMerchant(
                merchantId,
                queryParams.getCategoryId(),
                queryParams.getStatus(),
                queryParams.getKeyword()
        );
        Page<Product> productPage = (Page<Product>) products;
        return Result.success(new PageResult<>(productPage.getTotal(), productPage.getResult()));
    }

    @PostMapping("/products")
    public Result<String> addProduct(@RequestBody Product product) {
        Long merchantId = AuthContext.requireCurrentUserId();
        product.setMerchantId(merchantId);
        productMapper.insertProduct(product);
        return Result.success("商家商品新增成功");
    }

    @PutMapping("/products/{id}")
    public Result<String> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Long merchantId = AuthContext.requireCurrentUserId();
        product.setId(id);
        int affectedRows = productMapper.updateProductByMerchant(product, merchantId);
        if (affectedRows == 0) {
            throw new SecurityException("无权修改该商品");
        }
        return Result.success("商家商品修改成功");
    }

    @DeleteMapping("/products/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        Long merchantId = AuthContext.requireCurrentUserId();
        int affectedRows = productMapper.deleteProductByMerchant(id, merchantId);
        if (affectedRows == 0) {
            throw new SecurityException("无权删除该商品");
        }
        return Result.success("商家商品删除成功");
    }

    @GetMapping("/orders/category-stats")
    public Result<List<MerchantCategoryStats>> categoryStats() {
        Long merchantId = AuthContext.requireCurrentUserId();
        List<MerchantCategoryStats> stats = orderMapper.selectMerchantCategoryStats(merchantId);
        return Result.success(stats);
    }
}
