package com.xinjiema.hualimall.service.impl;

import com.xinjiema.hualimall.mapper.UserMapper;
import com.xinjiema.hualimall.pojo.LoginRequest;
import com.xinjiema.hualimall.pojo.LoginResponse;
import com.xinjiema.hualimall.pojo.User;
import com.xinjiema.hualimall.service.UserService;
import com.xinjiema.hualimall.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("注册参数不能为空");
        }
        if (isBlank(user.getUsername()) || isBlank(user.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        User existedUser = userMapper.selectByUsername(user.getUsername());
        if (existedUser != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (isBlank(user.getNickname())) {
            user.setNickname(user.getUsername());
        }
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        if (loginRequest == null || isBlank(loginRequest.getUsername()) || isBlank(loginRequest.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        if (user == null || !Objects.equals(user.getPassword(), loginRequest.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("用户已被禁用");
        }

        String token = JwtUtils.createToken(user.getId(), user.getUsername());
        return new LoginResponse(token);
    }

    @Override
    public User getUserInfo(Long userId) {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("用户ID非法");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
