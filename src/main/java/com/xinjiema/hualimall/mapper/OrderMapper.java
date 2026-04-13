package com.xinjiema.hualimall.mapper;

import com.xinjiema.hualimall.pojo.Order;
import com.xinjiema.hualimall.pojo.OrderItem;
import com.xinjiema.hualimall.pojo.ProQueryParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    void insertOrder(Order order);

    void insertOrderItems(@Param("items") List<OrderItem> items);

    List<Order> selectOrderPage(ProQueryParams params);

    Order selectById(Long id);

    List<OrderItem> selectItemsByOrderId(Long orderId);

    void updateOrderStatus(@Param("id") Long id, @Param("status") Integer status);
}

