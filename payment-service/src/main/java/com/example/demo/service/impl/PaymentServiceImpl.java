package com.example.demo.service.impl;

import com.example.demo.service.OrderService;
import com.example.demo.service.PaymentService;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Component
public class PaymentServiceImpl implements PaymentService {
    
    @Reference
    private OrderService orderService;
    
    private Map<String, Map<String, Object>> paymentStore = new ConcurrentHashMap<>();
    private Map<String, List<String>> userPaymentMap = new ConcurrentHashMap<>();
    private Map<String, String> orderPaymentMap = new ConcurrentHashMap<>();
    
    @Override
    @Tags({
        @Tag(key = "orderId", value = "arg[0]"),
        @Tag(key = "userId", value = "arg[1]"),
        @Tag(key = "amount", value = "arg[2]"),
        @Tag(key = "paymentMethod", value = "arg[3]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> createPayment(String orderId, String userId, double amount, String paymentMethod) {
        // 验证订单
        Map<String, Object> order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        
        // 检查是否已存在支付
        if (orderPaymentMap.containsKey(orderId)) {
            String existingPaymentId = orderPaymentMap.get(orderId);
            return paymentStore.get(existingPaymentId);
        }
        
        String paymentId = "PAY_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> payment = new HashMap<>();
        payment.put("paymentId", paymentId);
        payment.put("orderId", orderId);
        payment.put("userId", userId);
        payment.put("amount", amount);
        payment.put("paymentMethod", paymentMethod);
        payment.put("status", "PENDING");
        payment.put("createTime", new Date());
        payment.put("orderVerified", true);
        
        paymentStore.put(paymentId, payment);
        userPaymentMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(paymentId);
        orderPaymentMap.put(orderId, paymentId);
        
        // 模拟异步支付处理
        new Thread(() -> processPayment(paymentId)).start();
        
        logPaymentCreation(paymentId, orderId);
        return payment;
    }
    
    @Override
    @Tags({
        @Tag(key = "paymentId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> getPaymentStatus(String paymentId) {
        Map<String, Object> payment = paymentStore.get(paymentId);
        if (payment != null) {
            return enrichPaymentData(payment);
        }
        return null;
    }
    
    @Override
    @Tags({
        @Tag(key = "paymentId", value = "arg[0]"),
        @Tag(key = "status", value = "arg[1]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public boolean handlePaymentCallback(String paymentId, String status, Map<String, Object> callbackData) {
        Map<String, Object> payment = paymentStore.get(paymentId);
        if (payment != null) {
            payment.put("status", status);
            payment.put("callbackTime", new Date());
            payment.put("callbackData", callbackData);
            
            // 如果支付成功，更新订单状态
            if ("SUCCESS".equals(status)) {
                String orderId = (String) payment.get("orderId");
                orderService.updateOrderStatus(orderId, "PAID");
                payment.put("orderUpdated", true);
            }
            
            logPaymentCallback(paymentId, status);
            return true;
        }
        return false;
    }
    
    @Override
    @Tags({
        @Tag(key = "paymentId", value = "arg[0]"),
        @Tag(key = "amount", value = "arg[1]"),
        @Tag(key = "reason", value = "arg[2]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public Map<String, Object> refund(String paymentId, double amount, String reason) {
        Map<String, Object> payment = paymentStore.get(paymentId);
        if (payment == null || !"SUCCESS".equals(payment.get("status"))) {
            throw new IllegalArgumentException("Invalid payment for refund");
        }
        
        String refundId = "REFUND_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> refund = new HashMap<>();
        refund.put("refundId", refundId);
        refund.put("paymentId", paymentId);
        refund.put("amount", amount);
        refund.put("reason", reason);
        refund.put("status", "PROCESSING");
        refund.put("createTime", new Date());
        
        // 模拟退款处理
        new Thread(() -> processRefund(refundId)).start();
        
        // 更新订单状态
        String orderId = (String) payment.get("orderId");
        orderService.updateOrderStatus(orderId, "REFUNDING");
        
        logRefund(refundId, paymentId);
        return refund;
    }
    
    @Override
    @Tags({
        @Tag(key = "userId", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public List<Map<String, Object>> getUserPaymentHistory(String userId) {
        List<String> paymentIds = userPaymentMap.getOrDefault(userId, Collections.emptyList());
        List<Map<String, Object>> payments = new ArrayList<>();
        
        for (String paymentId : paymentIds) {
            Map<String, Object> payment = getPaymentStatus(paymentId);
            if (payment != null) {
                payments.add(payment);
            }
        }
        
        return payments;
    }
    
    @Override
    @Tags({
        @Tag(key = "orderId", value = "arg[0]"),
        @Tag(key = "userId", value = "arg[1]"),
        @Tag(key = "result", value = "returnedObj")
    })
    public boolean validatePayment(String orderId, String userId) {
        String paymentId = orderPaymentMap.get(orderId);
        if (paymentId != null) {
            Map<String, Object> payment = paymentStore.get(paymentId);
            return payment != null && userId.equals(payment.get("userId"));
        }
        return false;
    }
    
    @Tags({
        @Tag(key = "paymentId", value = "arg[0]"),
        @Tag(key = "orderId", value = "arg[1]")
    })
    private void logPaymentCreation(String paymentId, String orderId) {
        System.out.println("Payment created: " + paymentId + " for order: " + orderId);
    }
    
    @Tags({
        @Tag(key = "paymentId", value = "arg[0]"),
        @Tag(key = "status", value = "arg[1]")
    })
    private void logPaymentCallback(String paymentId, String status) {
        System.out.println("Payment callback received: " + paymentId + " status: " + status);
    }
    
    @Tags({
        @Tag(key = "refundId", value = "arg[0]"),
        @Tag(key = "paymentId", value = "arg[1]")
    })
    private void logRefund(String refundId, String paymentId) {
        System.out.println("Refund initiated: " + refundId + " for payment: " + paymentId);
    }
    
    @Tags({
        @Tag(key = "paymentId", value = "arg[0]")
    })
    private void processPayment(String paymentId) {
        // 模拟支付处理延迟
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, Object> payment = paymentStore.get(paymentId);
        if (payment != null) {
            // 80%概率支付成功
            boolean success = Math.random() < 0.8;
            payment.put("status", success ? "SUCCESS" : "FAILED");
            payment.put("processTime", new Date());
            
            if (success) {
                String orderId = (String) payment.get("orderId");
                orderService.updateOrderStatus(orderId, "PAID");
            }
        }
    }
    
    @Tags({
        @Tag(key = "refundId", value = "arg[0]")
    })
    private void processRefund(String refundId) {
        // 模拟退款处理延迟
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 这里简化处理，实际应该有退款记录存储
    }
    
    @Tags({
        @Tag(key = "payment", value = "arg[0]"),
        @Tag(key = "result", value = "returnedObj")
    })
    private Map<String, Object> enrichPaymentData(Map<String, Object> payment) {
        Map<String, Object> enriched = new HashMap<>(payment);
        // 添加一些额外信息
        String orderId = (String) payment.get("orderId");
        enriched.put("orderRef", orderId);
        enriched.put("transactionTime", payment.getOrDefault("processTime", payment.get("createTime")));
        return enriched;
    }
}