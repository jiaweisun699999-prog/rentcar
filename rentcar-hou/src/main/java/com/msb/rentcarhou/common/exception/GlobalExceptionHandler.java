package com.msb.rentcarhou.common.exception;

import com.msb.rentcarhou.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获自定义的业务异常 (如 RuntimeException 抛出的 "账号不存在")
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(500, e.getMessage());
    }

    /**
     * 捕获 SQL 数据库相关异常
     */
    @ExceptionHandler(SQLException.class)
    public Result<Void> handleSQLException(SQLException e) {
        log.error("数据库操作异常: ", e);
        // 为了安全起见，尽量不要把原始的 SQL 报错直接暴露给前端
        return Result.error(500, "数据库操作失败，请联系管理员");
    }

    /**
     * 捕获所有未知的 Exception
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统未知异常: ", e);
        return Result.error(500, "系统开小差了，请稍后再试");
    }
}
