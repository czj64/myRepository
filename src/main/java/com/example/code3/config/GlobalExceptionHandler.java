package com.example.code3.config;

import com.example.code3.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, Model model) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        model.addAttribute("errorCode", e.getCode());
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }

    /**
     * 处理404异常 - 静态资源未找到
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException e, Model model) {
        log.warn("静态资源未找到: {}", e.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "页面不存在");
        return "error";
    }

    /**
     * 处理404异常 - 数据未找到
     */
    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public String handleNotFoundException(ChangeSetPersister.NotFoundException e, Model model) {
        log.warn("资源未找到: {}", e.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "页面不存在");
        return "error";
    }

    /**
     * 处理其他异常（不暴露内部错误细节）
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("系统异常", e);
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "系统繁忙，请稍后重试");
        return "error";
    }
}
