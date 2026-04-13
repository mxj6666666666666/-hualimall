package com.xinjiema.hualimall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xinjiema.hualimall.mapper.OrderMapper;
import com.xinjiema.hualimall.mapper.ProductMapper;
import com.xinjiema.hualimall.pojo.*;
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

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Order createOrder(Order order) {

        if (order.getUserId() == null) {
            throw new RuntimeException("创建订单失败，用户ID不能为空");
        }

        // 生成订单号
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setStatus(0); // 0-待支付
        
        // 计算总金额 (从数据库获取最新真实价格，防止前端篡改)
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                Product product = productMapper.selectById(item.getProductId());
                if (product == null) {
                    throw new RuntimeException("商品不存在，ID: " + item.getProductId());
                }
                
                // 以数据库为准，覆盖前端传过来的商品名称和价格
                item.setProductName(product.getName());
                item.setProductPrice(product.getPrice());
                
                // 计算当前明细的总价
                item.setTotalPrice(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
                
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
    public PageResult<Order> getOrderList(OrdQueryParams ordQueryParams) {
        PageHelper.startPage(ordQueryParams.getPage(), ordQueryParams.getPageSize());
        List<Order> list = orderMapper.selectOrderPage(ordQueryParams);
        Page<Order> orderPage = (Page<Order>) list;
        
        // 遍历订单列表，为每个订单查询并设置对应的商品明细
        for (Order order : orderPage.getResult()) {
            List<OrderItem> items = orderMapper.selectItemsByOrderId(order.getId());
            order.setItems(items);
        }
        
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
        Order order = orderMapper.selectById(id);

        if (order == null){
            throw new RuntimeException("订单不存在");
        }
        if(order.getStatus() != 0){
            throw new RuntimeException("只能取消未支付订单");
        }

        orderMapper.updateOrderStatus(id, 2);
    }
}
