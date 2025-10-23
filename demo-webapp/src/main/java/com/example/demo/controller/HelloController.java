package com.example.demo.controller;

import com.example.demo.service.HelloService;
import com.example.demo.service.OrderService;
import com.example.demo.service.PaymentService;
import com.example.demo.service.AnalyticsService;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class HelloController {
    
    @Reference
    private HelloService helloService;
    
    @Reference
    private OrderService orderService;
    
    @Reference
    private PaymentService paymentService;
    
    @Reference
    private AnalyticsService analyticsService;
    
    @GetMapping("/hello/{name}")
    @Trace
    public String hello(@PathVariable String name) {
        return helloService.sayHello(name);
    }
    
    @GetMapping("/user/{id}")
    @Trace
    @Tags({@Tag(key = "id", value = "arg[0]"), @Tag(key = "result", value = "returnedObj")})
    public String getUserInfo(@PathVariable String id) {
        return helloService.getInfoById(id);
    }
    
    @GetMapping("/users")
    @Trace
    public List<String> getUsers() {
        return helloService.getUsers();
    }
    
    @PostMapping("/user")
    @Trace
    public Map<String, Object> createUser(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        return helloService.createUser(name, email);
    }
    
    @PutMapping("/user/{id}")
    @Trace
    public Map<String, Object> updateUser(@PathVariable String id, @RequestBody Map<String, Object> info) {
        boolean success = helloService.updateUser(id, info);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("userId", id);
        if (!success) {
            response.put("message", "User not found");
        }
        return response;
    }
    
    // 添加一个调用多个服务的复杂端点
    @GetMapping("/complex-flow/{name}")
    @Trace
    public Map<String, Object> complexFlow(@PathVariable String name) {
        Map<String, Object> result = new HashMap<>();
        
        // 调用多个服务方法，创建复杂的调用链路
        result.put("greeting", helloService.sayHello(name));
        
        // 创建一个用户
        Map<String, Object> user = helloService.createUser(name, name + "@example.com");
        result.put("createdUser", user);
        
        // 查询用户信息
        String userId = (String) user.get("id");
        result.put("userInfo", helloService.getInfoById(userId));
        
        // 获取用户列表
        result.put("userList", helloService.getUsers());
        
        // 更新用户信息
        Map<String, Object> updateInfo = new HashMap<>();
        updateInfo.put("name", name + " Updated");
        boolean updated = helloService.updateUser(userId, updateInfo);
        result.put("updateSuccess", updated);
        
        // 再次查询更新后的用户信息
        result.put("updatedUserInfo", helloService.getInfoById(userId));
        
        return result;
    }
    
    // 订单相关接口
    @PostMapping("/order")
    @Trace
    @Tags({
        @Tag(key = "userId", value = "arg[0]"),
        @Tag(key = "items", value = "arg[1]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> createOrder(@RequestParam String userId, @RequestBody List<Map<String, Object>> items) {
        return orderService.createOrder(userId, items);
    }
    
    @GetMapping("/order/{orderId}")
    @Trace
    @Tags({
        @Tag(key = "orderId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> getOrder(@PathVariable String orderId) {
        return orderService.getOrderById(orderId);
    }
    
    @GetMapping("/user/{userId}/orders")
    @Trace
    @Tags({
        @Tag(key = "userId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public List<Map<String, Object>> getUserOrders(@PathVariable String userId) {
        return orderService.getUserOrders(userId);
    }
    
    // 支付相关接口
    @PostMapping("/payment")
    @Trace
    @Tags({
        @Tag(key = "orderId", value = "arg[0]"),
        @Tag(key = "userId", value = "arg[1]"),
        @Tag(key = "amount", value = "arg[2]"),
        @Tag(key = "paymentMethod", value = "arg[3]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> createPayment(
            @RequestParam String orderId,
            @RequestParam String userId,
            @RequestParam double amount,
            @RequestParam String paymentMethod) {
        return paymentService.createPayment(orderId, userId, amount, paymentMethod);
    }
    
    @GetMapping("/payment/{paymentId}")
    @Trace
    @Tags({
        @Tag(key = "paymentId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> getPaymentStatus(@PathVariable String paymentId) {
        return paymentService.getPaymentStatus(paymentId);
    }
    
    // 数据分析相关接口
    @GetMapping("/analytics/user/{userId}")
    @Trace
    @Tags({
        @Tag(key = "userId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> getUserAnalytics(@PathVariable String userId) {
        return analyticsService.getUserAnalyticsReport(userId);
    }
    
    @GetMapping("/analytics/system")
    @Trace
    @Tags({
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> getSystemAnalytics() {
        return analyticsService.getSystemAnalyticsData();
    }
    
    // 超复杂调用链路示例
    @GetMapping("/super-complex-flow/{userId}")
    @Trace
    @Tags({
        @Tag(key = "userId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> superComplexFlow(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 创建用户
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", userId);
        userData.put("email", userId + "@example.com");
        Map<String, Object> createdUser = helloService.createUser(userId, userId + "@example.com");
        String createdUserId = (String) createdUser.get("id");
        Map<String, Object> user = new HashMap<>();
        user.put("id", createdUserId);
        user.put("name", userId);
        user.put("email", userId + "@example.com");
        result.put("user", user);
        
        // 2. 创建订单
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("productId", "P001");
        item1.put("name", "Demo Product");
        item1.put("price", 199.99);
        item1.put("quantity", 2);
        items.add(item1);
        
        Map<String, Object> order = orderService.createOrder(createdUserId, items);
        String orderId = (String) order.get("orderId");
        result.put("order", order);
        
        // 3. 创建支付
        Map<String, Object> payment = paymentService.createPayment(
                orderId, 
                createdUserId, 
                399.98, 
                "Credit Card"
        );
        String paymentId = (String) payment.get("paymentId");
        result.put("payment", payment);
        
        // 4. 处理支付回调
        Map<String, Object> callbackData = new HashMap<>();
        callbackData.put("transactionId", "TXN" + System.currentTimeMillis());
        callbackData.put("gateway", "Demo Gateway");
        boolean callbackResult = paymentService.handlePaymentCallback(paymentId, "SUCCESS", callbackData);
        result.put("paymentCallbackResult", callbackResult);
        
        // 5. 获取支付状态
        Map<String, Object> paymentStatus = paymentService.getPaymentStatus(paymentId);
        result.put("paymentStatus", paymentStatus);
        
        // 6. 获取用户分析报告
        Map<String, Object> analyticsReport = analyticsService.getUserAnalyticsReport(createdUserId);
        result.put("analyticsReport", analyticsReport);
        
        // 7. 获取系统分析数据
        Map<String, Object> systemAnalytics = analyticsService.getSystemAnalyticsData();
        result.put("systemAnalytics", systemAnalytics);
        
        // 8. 异步处理：创建另一个订单并获取销售趋势
        new Thread(() -> {
            try {
                // 创建第二个订单
                orderService.createOrder(createdUserId, items);
                // 获取销售趋势
                analyticsService.getSalesTrend();
            } catch (Exception e) {
                System.err.println("Async processing error: " + e.getMessage());
            }
        }).start();
        
        result.put("timestamp", new Date());
        result.put("flowStatus", "COMPLETED");
        
        return result;
    }
}