package com.example.demo.service;

import java.util.Map;

public interface PaymentService {
    // 创建支付
    Map<String, Object> createPayment(String orderId, String userId, double amount, String paymentMethod);
    
    // 查询支付状态
    Map<String, Object> getPaymentStatus(String paymentId);
    
    // 处理支付回调
    boolean handlePaymentCallback(String paymentId, String status, Map<String, Object> callbackData);
    
    // 退款
    Map<String, Object> refund(String paymentId, double amount, String reason);
    
    // 获取用户支付历史
    java.util.List<Map<String, Object>> getUserPaymentHistory(String userId);
    
    // 验证支付信息
    boolean validatePayment(String orderId, String userId);
}