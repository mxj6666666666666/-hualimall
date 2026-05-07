package com.xinjiema.hualimall.service.impl;

import com.xinjiema.hualimall.mapper.UserMapper;
import com.xinjiema.hualimall.pojo.LoginRequest;
import com.xinjiema.hualimall.pojo.LoginResponse;
import com.xinjiema.hualimall.pojo.RegisterRequest;
import com.xinjiema.hualimall.pojo.User;
import com.xinjiema.hualimall.service.UserService;
import com.xinjiema.hualimall.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new IllegalArgumentException("注册参数不能为空");
        }
        if (isBlank(registerRequest.getUsername()) || isBlank(registerRequest.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        User existedUser = userMapper.selectByUsername(registerRequest.getUsername());
        if (existedUser != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        String role = normalizeRole(registerRequest.getRole());
        if ("ADMIN".equals(role)) {
            throw new IllegalArgumentException("平台管理员不允许自助注册");
        }
        if (!"BUYER".equals(role) && !"MERCHANT".equals(role)) {
            throw new IllegalArgumentException("仅支持注册买家或商家账号");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setNickname(isBlank(registerRequest.getNickname()) ? registerRequest.getUsername() : registerRequest.getNickname());
        user.setAvatarUrl(registerRequest.getAvatarUrl());
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);

        if ("BUYER".equals(role)) {
            if (isBlank(registerRequest.getRealName()) || isBlank(registerRequest.getPhone())) {
                throw new IllegalArgumentException("买家注册需填写真实姓名和手机号");
            }
            userMapper.insertBuyerProfile(user.getId(), registerRequest.getRealName().trim(), registerRequest.getPhone().trim());
            return;
        }

        if (isBlank(registerRequest.getShopName())
                || isBlank(registerRequest.getBusinessLicenseNo())
                || isBlank(registerRequest.getContactName())
                || isBlank(registerRequest.getContactPhone())) {
            throw new IllegalArgumentException("商家注册需填写店铺名、营业执照号、联系人和联系电话");
        }
        userMapper.insertMerchantProfile(
                user.getId(),
                registerRequest.getShopName().trim(),
                registerRequest.getBusinessLicenseNo().trim(),
                registerRequest.getContactName().trim(),
                registerRequest.getContactPhone().trim()
        );
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
        user.setRole(normalizeRole(user.getRole()));

        String token = jwtUtils.createToken(user.getId(), user.getUsername(), user.getRole());
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
        user.setRole(normalizeRole(user.getRole()));
        if ("BUYER".equals(user.getRole())) {
            User buyerProfile = userMapper.selectBuyerProfileByUserId(userId);
            if (buyerProfile != null) {
                user.setRealName(buyerProfile.getRealName());
                user.setPhone(buyerProfile.getPhone());
            }
        } else if ("MERCHANT".equals(user.getRole())) {
            User merchantProfile = userMapper.selectMerchantProfileByUserId(userId);
            if (merchantProfile != null) {
                user.setShopName(merchantProfile.getShopName());
                user.setBusinessLicenseNo(merchantProfile.getBusinessLicenseNo());
                user.setContactName(merchantProfile.getContactName());
                user.setContactPhone(merchantProfile.getContactPhone());
            }
        }
        user.setPassword(null);
        return user;
    }

    private String normalizeRole(String role) {
        if (isBlank(role)) {
            return "BUYER";
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return "USER".equals(normalized) ? "BUYER" : normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
