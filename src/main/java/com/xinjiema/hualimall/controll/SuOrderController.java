package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.OrderStatusUpdateRequest;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping({"/admin/orders", "/admin/order"})
public class SuOrderController {

    @Autowired
    private OrderService orderService;

    @PutMapping({"/{id}/status", "/{id}/status/update"})
    public Result<String> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        log.info("管理端更新订单状态，id: {}, status: {}", id, request.getStatus());
        orderService.updateOrderStatus(id, request.getStatus());
        return Result.success("订单状态更新成功");
    }
}
