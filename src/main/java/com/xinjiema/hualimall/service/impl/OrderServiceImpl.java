package com.xinjiema.hualimall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xinjiema.hualimall.mapper.OrderMapper;
import com.xinjiema.hualimall.pojo.Order;
import com.xinjiema.hualimall.pojo.OrderItem;
import com.xinjiema.hualimall.pojo.PageResult;
import com.xinjiema.hualimall.pojo.ProQueryParams;
import com.xinjiema.hualimall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Order createOrder(Order order) {
        // 生成订单号
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setStatus(0); // 0-待支付
        
        // 计算总金额 (可以根据 items 计算)
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                if (item.getTotalPrice() == null) {
                    item.setTotalPrice(item.getProductPrice().multiply(new BigDecimal(item.getQuantity())));
                }
                totalAmount = totalAmount.add(item.getTotalPrice());
            }
        }
        order.setTotalAmount(totalAmount);

        // 插入订单表
        orderMapper.insertOrder(order);

        // 插入订单明细表
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
            }
            orderMapper.insertOrderItems(order.getItems());
        }
        return order;
    }

    @Override
    public PageResult<Order> getOrderList(ProQueryParams proQueryParams) {
        PageHelper.startPage(proQueryParams.getPage(), proQueryParams.getPageSize());
        List<Order> list = orderMapper.selectOrderPage(proQueryParams);
        Page<Order> orderPage = (Page<Order>) list;
        return new PageResult<>(orderPage.getTotal(), orderPage.getResult());
    }

    @Override
    public Order getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            List<OrderItem> items = orderMapper.selectItemsByOrderId(id);
            order.setItems(items);
        }
        return order;
    }

    @Override
    public void cancelOrder(Long id) {
        // 更新订单状态为 取消 (2)
        orderMapper.updateOrderStatus(id, 2);
    }
}

