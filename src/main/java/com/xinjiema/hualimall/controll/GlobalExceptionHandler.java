package com.xinjiema.hualimall.controll;

import com.xinjiema.hualimall.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("请求参数或业务校验失败: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public Result<String> handleSecurityException(SecurityException e) {
        log.warn("鉴权失败: {}", e.getMessage());
        return Result.error(401, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("业务执行失败", e);
        return Result.error(e.getMessage());
    }
}
