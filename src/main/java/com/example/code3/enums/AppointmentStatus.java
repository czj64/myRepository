package com.example.code3.enums;

/**
 * 预约状态枚举
 */
public enum AppointmentStatus {
    PENDING("待就诊"),
    CONFIRMED("已确认"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    EXPIRED("已过期");
    
    private final String description;
    
    AppointmentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
