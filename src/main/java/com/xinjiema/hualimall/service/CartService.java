package com.xinjiema.hualimall.service;

import com.xinjiema.hualimall.pojo.CartAddRequest;
import com.xinjiema.hualimall.pojo.CartItem;
import com.xinjiema.hualimall.pojo.CartUpdateRequest;

import java.util.List;

public interface CartService {
    List<CartItem> getCartList();

    void add(CartAddRequest request);

    void update(Long id, CartUpdateRequest request);

    void remove(Long id);
}
