package com.xinjiema.hualimall.service.impl;

import com.xinjiema.hualimall.mapper.CartMapper;
import com.xinjiema.hualimall.mapper.ProductMapper;
import com.xinjiema.hualimall.pojo.CartAddRequest;
import com.xinjiema.hualimall.pojo.CartItem;
import com.xinjiema.hualimall.pojo.CartUpdateRequest;
import com.xinjiema.hualimall.pojo.Product;
import com.xinjiema.hualimall.service.CartService;
import com.xinjiema.hualimall.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<CartItem> getCartList() {
        Long userId = requireCurrentUserId();
        return cartMapper.selectCartListByUserId(userId);
    }

    @Override
    public void add(CartAddRequest request) {
        Long userId = requireCurrentUserId();
        if (request == null || request.getProductId() == null || request.getProductId() < 1) {
            throw new IllegalArgumentException("商品ID非法");
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new IllegalArgumentException("数量必须大于0");
        }

        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (product.getStatus() != null && product.getStatus() == 0) {
            throw new IllegalArgumentException("商品已下架");
        }

        CartItem existed = cartMapper.selectByUserIdAndProductId(userId, request.getProductId());
        if (existed == null) {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setSelected(1);
            cartMapper.insert(cartItem);
            return;
        }

        int newQuantity = existed.getQuantity() + request.getQuantity();
        cartMapper.updateQuantityAndSelected(existed.getId(), userId, newQuantity, 1);
    }

    @Override
    public void update(Long id, CartUpdateRequest request) {
        Long userId = requireCurrentUserId();
        if (id == null || id < 1) {
            throw new IllegalArgumentException("购物车ID非法");
        }
        if (request == null) {
            throw new IllegalArgumentException("更新参数不能为空");
        }
        if (request.getQuantity() != null && request.getQuantity() < 1) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        if (request.getSelected() != null && request.getSelected() != 0 && request.getSelected() != 1) {
            throw new IllegalArgumentException("selected 仅支持 0 或 1");
        }
        if (request.getQuantity() == null && request.getSelected() == null) {
            throw new IllegalArgumentException("至少需要更新一个字段");
        }

        int affectedRows = cartMapper.updateQuantityAndSelected(id, userId, request.getQuantity(), request.getSelected());
        if (affectedRows == 0) {
            throw new IllegalArgumentException("购物车项不存在");
        }
    }

    @Override
    public void remove(Long id) {
        Long userId = requireCurrentUserId();
        if (id == null || id < 1) {
            throw new IllegalArgumentException("购物车ID非法");
        }
        int affectedRows = cartMapper.deleteByIdAndUserId(id, userId);
        if (affectedRows == 0) {
            throw new IllegalArgumentException("购物车项不存在");
        }
    }

    private Long requireCurrentUserId() {
        Long userId = AuthContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("请先登录");
        }
        return userId;
    }
}
