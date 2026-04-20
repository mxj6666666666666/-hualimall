package com.xinjiema.hualimall.mapper;

import com.xinjiema.hualimall.pojo.OrdQueryParams;
import com.xinjiema.hualimall.pojo.Order;
import com.xinjiema.hualimall.pojo.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    void insertOrder(Order order);

    void insertOrderItems(@Param("items") List<OrderItem> items);

    List<Order> selectOrderPage(OrdQueryParams params);

    Order selectById(Long id);

    List<OrderItem> selectItemsByOrderId(@Param("orderId") Long orderId);

    void updateOrderStatus(@Param("id") Long id, @Param("status") Integer status);
}
