package com.xinjiema.hualimall.service;

import com.xinjiema.hualimall.pojo.Order;
import com.xinjiema.hualimall.pojo.PageResult;
import com.xinjiema.hualimall.pojo.ProQueryParams;

public interface OrderService {

    // 创建订单
    Order createOrder(Order order);

    // 查询订单列表
    PageResult<Order> getOrderList(ProQueryParams proQueryParams);

    // 获取订单详情
    Order getOrderById(Long id);

    // 取消订单
    void cancelOrder(Long id);
}

