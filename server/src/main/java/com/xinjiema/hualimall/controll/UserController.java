package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.config.AuthCookieProperties;
import com.xinjiema.hualimall.pojo.LoginRequest;
import com.xinjiema.hualimall.pojo.LoginResponse;
import com.xinjiema.hualimall.pojo.RegisterRequest;
import com.xinjiema.hualimall.pojo.Result;
import com.xinjiema.hualimall.pojo.User;
import com.xinjiema.hualimall.service.UserService;
import com.xinjiema.hualimall.utils.AuthContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping({"/users", "/user"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthCookieProperties authCookieProperties;

    @PostMapping({"", "/register"})
    public Result<String> register(@RequestBody RegisterRequest registerRequest) {
        log.info("用户注册，用户名：{}，角色：{}", registerRequest == null ? null : registerRequest.getUsername(), registerRequest == null ? null : registerRequest.getRole());
        userService.register(registerRequest);
        return Result.success("注册成功");
    }

    @PostMapping({"/sessions", "/login"})
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("用户登录，用户名：{}", loginRequest == null ? null : loginRequest.getUsername());
        LoginResponse loginResponse = userService.login(loginRequest);
        ResponseCookie cookie = ResponseCookie.from(authCookieProperties.getName(), loginResponse.getToken())
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .path("/")
                .sameSite(authCookieProperties.getSameSite())
                .maxAge(Duration.ofSeconds(authCookieProperties.getMaxAgeSeconds()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return Result.success(new LoginResponse(null));
    }

    @DeleteMapping({"/sessions", "/logout"})
    public Result<String> logout(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from(authCookieProperties.getName(), "")
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .path("/")
                .sameSite(authCookieProperties.getSameSite())
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
        return Result.success("退出成功");
    }

    @GetMapping({"/me", "/info"})
    public Result<User> getUserInfo() {
        Long currentUserId = AuthContext.getCurrentUserId();
        User userInfo = userService.getUserInfo(currentUserId);
        return Result.success(userInfo);
    }
}
