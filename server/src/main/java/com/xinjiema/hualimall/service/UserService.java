package com.xinjiema.hualimall.service;

import com.xinjiema.hualimall.pojo.LoginRequest;
import com.xinjiema.hualimall.pojo.LoginResponse;
import com.xinjiema.hualimall.pojo.User;

public interface UserService {
    void register(User user);

    LoginResponse login(LoginRequest loginRequest);

    User getUserInfo(Long userId);
}
