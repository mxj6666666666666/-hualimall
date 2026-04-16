package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.CartAddRequest;
import com.xinjiema.hualimall.pojo.CartItem;
import com.xinjiema.hualimall.pojo.CartUpdateRequest;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/carts", "/cart"})
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping({"", "/list"})
    public Result<List<CartItem>> list() {
        List<CartItem> cartItems = cartService.getCartList();
        return Result.success(cartItems);
    }

    @PostMapping({"", "/add"})
    public Result<String> add(@RequestBody CartAddRequest request) {
        log.info("添加购物车，参数：{}", request);
        cartService.add(request);
        return Result.success("添加成功");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody CartUpdateRequest request) {
        log.info("更新购物车，id: {}, 参数：{}", id, request);
        cartService.update(id, request);
        return Result.success("更新成功");
    }

    @PutMapping("/update")
    public Result<String> updateLegacy(@RequestParam Long id, @RequestBody CartUpdateRequest request) {
        log.info("更新购物车(兼容接口)，id: {}, 参数：{}", id, request);
        cartService.update(id, request);
        return Result.success("更新成功");
    }

    @DeleteMapping({"/{id}", "/remove/{id}"})
    public Result<String> remove(@PathVariable Long id) {
        log.info("删除购物车项，id: {}", id);
        cartService.remove(id);
        return Result.success("删除成功");
    }
}
