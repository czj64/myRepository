package com.example.code3.config;

import com.example.code3.exception.BusinessException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常处理器
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, Model model) {
        model.addAttribute("errorCode", e.getCode());
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public String handleNotFoundException(ChangeSetPersister.NotFoundException e, Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "页面不存在：" + e.getMessage());
        return "error";
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "系统错误：" + e.getMessage());
        return "error";
    }
}
