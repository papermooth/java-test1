package com.example.demo.service.impl;

import com.example.demo.service.HelloService;
import com.example.demo.service.OrderService;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Component
public class OrderServiceImpl implements OrderService {
    
    @Reference
    private HelloService helloService;
    
    private Map<String, Map<String, Object>> orderStore = new ConcurrentHashMap<>();
    private Map<String, List<String>> userOrderMap = new ConcurrentHashMap<>();
    
    @Override
    @Tags({
        @Tag(key = "userId", value = "arg[0]"),
        @Tag(key = "itemCount", value = "arg[1].size()"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> createOrder(String userId, List<Map<String, Object>> items) {
        // 生成订单ID
        String orderId = "ORDER_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", userId);
        order.put("items", items);
        order.put("createTime", new Date());
        order.put("status", "PENDING_PAYMENT");
        order.put("totalAmount", calculateTotal(items));
        order.put("userVerified", true); // 用户验证在系统其他层完成
        
        orderStore.put(orderId, order);
        userOrderMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(orderId);
        
        logOrderCreation(orderId, userId);
        return order;
    }
    
    @Override
    @Tags({
        @Tag(key = "orderId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> getOrderById(String orderId) {
        Map<String, Object> order = orderStore.get(orderId);
        if (order != null) {
            // 调用内部方法处理订单数据
            return enrichOrderData(order);
        }
        return null;
    }
    
    @Override
    @Tags({
        @Tag(key = "userId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public List<Map<String, Object>> getUserOrders(String userId) {
        List<String> orderIds = userOrderMap.getOrDefault(userId, Collections.emptyList());
        List<Map<String, Object>> orders = new ArrayList<>();
        
        for (String orderId : orderIds) {
            Map<String, Object> order = getOrderById(orderId);
            if (order != null) {
                orders.add(order);
            }
        }
        
        return orders;
    }
    
    @Override
    @Tags({
        @Tag(key = "orderId", value = "arg[0]"),
        @Tag(key = "status", value = "arg[1]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public boolean updateOrderStatus(String orderId, String status) {
        Map<String, Object> order = orderStore.get(orderId);
        if (order != null) {
            order.put("status", status);
            order.put("updateTime", new Date());
            
            // 模拟异步日志记录
            new Thread(() -> logOrderUpdate(orderId, status)).start();
            return true;
        }
        return false;
    }
    
    @Override
    @Tags({
        @Tag(key = "orderId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public boolean cancelOrder(String orderId) {
        return updateOrderStatus(orderId, "CANCELLED");
    }
    
    @Override
    @Tags({
        @Tag(key = "userId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> getOrderStatistics(String userId) {
        List<Map<String, Object>> orders = getUserOrders(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orders.size());
        
        Map<String, Long> statusCount = new HashMap<>();
        double totalAmount = 0;
        
        for (Map<String, Object> order : orders) {
            String status = (String) order.get("status");
            statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);
            totalAmount += (Double) order.get("totalAmount");
        }
        
        stats.put("statusDistribution", statusCount);
        stats.put("totalSpent", totalAmount);
        stats.put("lastOrderTime", orders.isEmpty() ? null : orders.get(0).get("createTime"));
        
        return stats;
    }
    
    @Tags({
        @Tag(key = "orderId", value = "arg[0]"),
        @Tag(key = "userId", value = "arg[1]")
    })
    private void logOrderCreation(String orderId, String userId) {
        // 模拟日志记录
        System.out.println("Order created: " + orderId + " for user: " + userId);
    }
    
    @Tags({
        @Tag(key = "orderId", value = "arg[0]"),
        @Tag(key = "status", value = "arg[1]")
    })
    private void logOrderUpdate(String orderId, String status) {
        // 模拟日志记录
        System.out.println("Order updated: " + orderId + " to status: " + status);
    }
    
    @Tags({
        @Tag(key = "order", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    private Map<String, Object> enrichOrderData(Map<String, Object> order) {
        Map<String, Object> enriched = new HashMap<>(order);
        // 添加一些额外信息
        enriched.put("paymentStatus", "UNPAID");
        enriched.put("shippingStatus", "NOT_SHIPPED");
        enriched.put("orderNumber", generateOrderNumber((String) order.get("orderId")));
        return enriched;
    }
    
    private double calculateTotal(List<Map<String, Object>> items) {
        return items.stream()
                .mapToDouble(item -> {
                    double price = (Double) item.getOrDefault("price", 0.0);
                    int quantity = (Integer) item.getOrDefault("quantity", 1);
                    return price * quantity;
                })
                .sum();
    }
    
    private String generateOrderNumber(String orderId) {
        return "ORD-" + System.currentTimeMillis() + "-" + orderId.substring(orderId.length() - 4);
    }
}