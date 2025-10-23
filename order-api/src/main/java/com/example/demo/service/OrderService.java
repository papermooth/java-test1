package com.example.demo.service;

import java.util.List;
import java.util.Map;

public interface OrderService {
    // 创建订单
    Map<String, Object> createOrder(String userId, List<Map<String, Object>> items);
    
    // 获取订单详情
    Map<String, Object> getOrderById(String orderId);
    
    // 获取用户订单列表
    List<Map<String, Object>> getUserOrders(String userId);
    
    // 更新订单状态
    boolean updateOrderStatus(String orderId, String status);
    
    // 取消订单
    boolean cancelOrder(String orderId);
    
    // 获取订单统计信息
    Map<String, Object> getOrderStatistics(String userId);
}