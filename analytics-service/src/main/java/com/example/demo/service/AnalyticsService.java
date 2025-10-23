package com.example.demo.service;

import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    // 获取用户综合分析报告
    Map<String, Object> getUserAnalyticsReport(String userId);
    
    // 获取系统整体分析数据
    Map<String, Object> getSystemAnalyticsData();
    
    // 获取销售趋势分析
    List<Map<String, Object>> getSalesTrend();
    
    // 获取用户行为分析
    Map<String, Object> getUserBehaviorAnalysis(String userId);
    
    // 获取支付方式分析
    Map<String, Object> getPaymentMethodAnalysis();
}