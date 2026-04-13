package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.Order;
import com.xinjiema.hualimall.pojo.PageResult;
import com.xinjiema.hualimall.pojo.ProQueryParams;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody Order order) {
        log.info("创建订单，参数：{}", order);
        Order createdOrder = orderService.createOrder(order);
        return Result.success(createdOrder);
    }

    @GetMapping("/list")
    public Result<PageResult<Order>> list(ProQueryParams proQueryParams) {
        log.info("查询订单列表，参数：{}", proQueryParams);
        PageResult<Order> result = orderService.getOrderList(proQueryParams);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Order> findById(@PathVariable Long id) {
        log.info("查询 id = {} 的订单详情", id);
        Order order = orderService.getOrderById(id);
        return Result.success(order);
    }

    @PutMapping("/cancel/{id}")
    public Result<String> cancelOrder(@PathVariable Long id) {
        log.info("取消 id = {} 的订单", id);
        orderService.cancelOrder(id);
        return Result.success("订单取消成功");
    }
}

