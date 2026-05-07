package com.xinjiema.hualimall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xinjiema.hualimall.mapper.CartMapper;
import com.xinjiema.hualimall.mapper.OrderMapper;
import com.xinjiema.hualimall.mapper.ProductMapper;
import com.xinjiema.hualimall.pojo.*;
import com.xinjiema.hualimall.service.OrderService;
import com.xinjiema.hualimall.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;

    @Override
    @Transactional
    public Order createOrder(Order order) {

        if (order == null) {
            throw new IllegalArgumentException("创建订单失败，请求体不能为空");
        }
        String role = AuthContext.requireCurrentUserRole();
        if (!"BUYER".equals(role)) {
            throw new SecurityException("仅买家可创建订单");
        }
        if (order.getUserId() == null) {
            Long currentUserId = AuthContext.getCurrentUserId();
            if (currentUserId != null) {
                order.setUserId(currentUserId);
            }
        }
        if (order.getUserId() == null) {
            throw new IllegalArgumentException("创建订单失败，用户ID不能为空");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            List<CartItem> selectedCartItems = cartMapper.selectSelectedByUserId(order.getUserId());
            if (selectedCartItems == null || selectedCartItems.isEmpty()) {
                throw new IllegalArgumentException("创建订单失败，订单明细不能为空");
            }
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : selectedCartItems) {
                orderItems.add(new OrderItem(null, null, cartItem.getProductId(), null, null, cartItem.getQuantity(), null));
            }
            order.setItems(orderItems);
        }

        // 生成订单号
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setStatus(0); // 0-待支付
        
        // 计算总金额 (从数据库获取最新真实价格，防止前端篡改)
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null) {
                throw new IllegalArgumentException("创建订单失败，商品ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("创建订单失败，商品数量必须大于0");
            }

            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("商品不存在，ID: " + item.getProductId());
            }

            int affectedRows = productMapper.decreaseStock(product.getId(), item.getQuantity());
            if (affectedRows == 0) {
                throw new IllegalArgumentException("库存不足，商品ID: " + item.getProductId());
            }

            // 以数据库为准，覆盖前端传过来的商品名称和价格
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());

            // 计算当前明细的总价
            item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        order.setTotalAmount(totalAmount);

        // 插入订单表
        orderMapper.insertOrder(order);

        // 插入订单明细表
        for (OrderItem item : order.getItems()) {
            item.setOrderId(order.getId());
        }
        orderMapper.insertOrderItems(order.getItems());
        cartMapper.deleteSelectedByUserId(order.getUserId());
        return order;
    }

    @Override
    public PageResult<Order> getOrderList(OrdQueryParams ordQueryParams) {
        if (ordQueryParams.getPage() == null || ordQueryParams.getPage() < 1) {
            throw new IllegalArgumentException("page 必须大于等于 1");
        }
        if (ordQueryParams.getPageSize() == null || ordQueryParams.getPageSize() < 1) {
            throw new IllegalArgumentException("pageSize 必须大于等于 1");
        }
        String role = AuthContext.requireCurrentUserRole();
        Long currentUserId = AuthContext.requireCurrentUserId();
        PageHelper.startPage(ordQueryParams.getPage(), ordQueryParams.getPageSize());
        List<Order> list;
        if ("ADMIN".equals(role)) {
            list = orderMapper.selectOrderPage(ordQueryParams);
        } else if ("MERCHANT".equals(role)) {
            list = orderMapper.selectOrderPageByMerchantId(currentUserId);
        } else {
            list = orderMapper.selectOrderPageByUserId(currentUserId);
        }
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
        if (id == null || id < 1) {
            throw new IllegalArgumentException("订单ID非法");
        }
        String role = AuthContext.requireCurrentUserRole();
        Long currentUserId = AuthContext.requireCurrentUserId();
        Order order;
        if ("ADMIN".equals(role)) {
            order = orderMapper.selectById(id);
        } else if ("MERCHANT".equals(role)) {
            order = orderMapper.selectByIdByMerchantId(id, currentUserId);
        } else {
            order = orderMapper.selectById(id);
            if (order != null && !currentUserId.equals(order.getUserId())) {
                throw new SecurityException("无权查看该订单");
            }
        }
        if (order != null) {
            List<OrderItem> items = orderMapper.selectItemsByOrderId(id);
            order.setItems(items);
        }
        return order;
    }

    @Override
    public void cancelOrder(Long id) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("订单ID非法");
        }
        String role = AuthContext.requireCurrentUserRole();
        if (!"BUYER".equals(role)) {
            throw new SecurityException("仅买家可取消订单");
        }
        Long currentUserId = AuthContext.requireCurrentUserId();
        // 更新订单状态为 取消 (2)
        Order order = orderMapper.selectById(id);

        if (order == null){
            throw new IllegalArgumentException("订单不存在");
        }
        if (!currentUserId.equals(order.getUserId())) {
            throw new SecurityException("无权取消该订单");
        }
        if(order.getStatus() != 0){
            throw new IllegalArgumentException("只能取消未支付订单");
        }

        orderMapper.updateOrderStatus(id, 2);
    }

    @Override
    public void updateOrderStatus(Long id, Integer status) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("订单ID非法");
        }
        if (status == null || status < 0 || status > 3) {
            throw new IllegalArgumentException("订单状态非法，仅支持 0~3");
        }
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        orderMapper.updateOrderStatus(id, status);
    }
}
