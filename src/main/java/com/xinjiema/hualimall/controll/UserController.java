package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.LoginRequest;
import com.xinjiema.hualimall.pojo.LoginResponse;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.pojo.User;
import com.xinjiema.hualimall.service.UserService;
import com.xinjiema.hualimall.utils.AuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping({"/users", "/user"})
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping({"", "/register"})
    public Result<String> register(@RequestBody User user) {
        log.info("用户注册，用户名：{}", user == null ? null : user.getUsername());
        userService.register(user);
        return Result.success("注册成功");
    }

    @PostMapping({"/sessions", "/login"})
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("用户登录，用户名：{}", loginRequest == null ? null : loginRequest.getUsername());
        LoginResponse loginResponse = userService.login(loginRequest);
        return Result.success(loginResponse);
    }

    @GetMapping({"/me", "/info"})
    public Result<User> getUserInfo() {
        Long currentUserId = AuthContext.getCurrentUserId();
        User userInfo = userService.getUserInfo(currentUserId);
        return Result.success(userInfo);
    }
}
