package com.xinjiema.hualimall.mapper;

import com.xinjiema.hualimall.pojo.OrdQueryParams;
import com.xinjiema.hualimall.pojo.Order;
import com.xinjiema.hualimall.pojo.OrderItem;
import com.xinjiema.hualimall.pojo.MerchantCategoryStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    void insertOrder(Order order);

    void insertOrderItems(@Param("items") List<OrderItem> items);

    List<Order> selectOrderPage(OrdQueryParams params);

    List<Order> selectOrderPageByUserId(@Param("userId") Long userId);

    List<Order> selectOrderPageByMerchantId(@Param("merchantId") Long merchantId);

    Order selectById(Long id);

    Order selectByIdByMerchantId(@Param("id") Long id, @Param("merchantId") Long merchantId);

    List<OrderItem> selectItemsByOrderId(@Param("orderId") Long orderId);

    void updateOrderStatus(@Param("id") Long id, @Param("status") Integer status);

    List<MerchantCategoryStats> selectMerchantCategoryStats(@Param("merchantId") Long merchantId);
}
