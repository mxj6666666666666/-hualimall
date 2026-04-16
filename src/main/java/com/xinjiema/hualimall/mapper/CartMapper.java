package com.xinjiema.hualimall.mapper;

import com.xinjiema.hualimall.pojo.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {

    List<CartItem> selectCartListByUserId(@Param("userId") Long userId);

    CartItem selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    void insert(CartItem cartItem);

    int updateQuantityAndSelected(@Param("id") Long id,
                                  @Param("userId") Long userId,
                                  @Param("quantity") Integer quantity,
                                  @Param("selected") Integer selected);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<CartItem> selectSelectedByUserId(@Param("userId") Long userId);

    int deleteSelectedByUserId(@Param("userId") Long userId);
}
