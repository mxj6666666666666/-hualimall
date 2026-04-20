package com.xinjiema.hualimall.service;

import com.xinjiema.hualimall.pojo.OrdQueryParams;
import com.xinjiema.hualimall.pojo.Order;
import com.xinjiema.hualimall.pojo.PageResult;

public interface OrderService {

    // 创建订单
    Order createOrder(Order order);

    // 查询订单列表
    PageResult<Order> getOrderList(OrdQueryParams ordQueryParams);

    // 获取订单详情
    Order getOrderById(Long id);

    // 取消订单
    void cancelOrder(Long id);

    // 管理端更新订单状态
    void updateOrderStatus(Long id, Integer status);
}

