package com.example.demo.service.impl;

import com.example.demo.service.HelloService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@DubboService
@Component
public class HelloServiceImpl implements HelloService {
    
    // 模拟内存数据库
    private final Map<String, Map<String, Object>> userDatabase = new ConcurrentHashMap<>();
    // 模拟缓存
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    @Trace
    @Tags({@Tag(key = "name", value = "arg[0]"), @Tag(key = "result", value = "returnedObj")})
    public String sayHello(String name) {
        // 添加内部方法调用以增强链路
        logRequest("sayHello", name);
        return "Hello, " + name + "! From Dubbo Provider";
    }
    
    @Trace
    @Tags({@Tag(key = "id", value = "arg[0]"), @Tag(key = "result", value = "returnedObj")})
    public String getInfoById(String id) {
        logRequest("getInfoById", id);
        
        // 先查缓存
        String cachedResult = queryCache(id);
        if (cachedResult != null) {
            return "From Cache: " + cachedResult;
        }
        
        // 缓存未命中，查询数据库
        String result = queryDatabase(id);
        
        // 写入缓存
        updateCache(id, result);
        
        return "From DB: " + result;
    }
    
    @Trace
    @Tags({@Tag(key = "result", value = "returnedObj")})
    public List<String> getUsers() {
        logRequest("getUsers", "all");
        // 模拟复杂的数据库查询操作
        return queryUserListFromDatabase();
    }
    
    @Trace
    @Tags({@Tag(key = "name", value = "arg[0]"), @Tag(key = "email", value = "arg[1]"), @Tag(key = "result", value = "returnedObj")})
    public Map<String, Object> createUser(String name, String email) {
        logRequest("createUser", name);
        // 模拟创建用户操作
        String userId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userId);
        userInfo.put("name", name);
        userInfo.put("email", email);
        userInfo.put("createdAt", new Date().toString());
        
        // 保存到数据库
        saveToDatabase(userId, userInfo);
        
        return userInfo;
    }
    
    @Trace
    @Tags({@Tag(key = "id", value = "arg[0]"), @Tag(key = "info", value = "arg[1]"), @Tag(key = "result", value = "returnedObj")})
    public boolean updateUser(String id, Map<String, Object> info) {
        logRequest("updateUser", id);
        
        // 检查用户是否存在
        if (!userDatabase.containsKey(id)) {
            return false;
        }
        
        // 更新数据库
        updateDatabase(id, info);
        
        // 删除缓存
        evictCache(id);
        
        return true;
    }
    
    // 模拟日志记录
    @Trace
    @Tags({@Tag(key = "method", value = "arg[0]"), @Tag(key = "param", value = "arg[1]")})
    private void logRequest(String method, String param) {
        // 模拟内部方法调用，增强链路追踪
        try {
            Thread.sleep(1); // 模拟延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // 模拟缓存查询
    @Trace
    @Tags({@Tag(key = "key", value = "arg[0]"), @Tag(key = "result", value = "returnedObj")})
    private String queryCache(String key) {
        try {
            Thread.sleep(2); // 模拟延迟
            return cache.get(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    // 模拟缓存更新
    @Trace
    @Tags({@Tag(key = "key", value = "arg[0]"), @Tag(key = "value", value = "arg[1]")})
    private void updateCache(String key, String value) {
        try {
            Thread.sleep(1); // 模拟延迟
            cache.put(key, value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // 模拟缓存删除
    @Trace
    @Tags({@Tag(key = "key", value = "arg[0]")})
    private void evictCache(String key) {
        try {
            Thread.sleep(1); // 模拟延迟
            cache.remove(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // 模拟数据库查询
    @Trace
    @Tags({@Tag(key = "id", value = "arg[0]"), @Tag(key = "result", value = "returnedObj")})
    private String queryDatabase(String id) {
        try {
            Thread.sleep(10); // 模拟数据库延迟
            
            if (userDatabase.containsKey(id)) {
                Map<String, Object> user = userDatabase.get(id);
                return "User: " + user.get("name") + ", Email: " + user.get("email");
            }
            return "User not found: " + id;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error occurred";
        }
    }
    
    // 模拟用户列表查询
    @Trace
    @Tags({@Tag(key = "result", value = "returnedObj")})
    private List<String> queryUserListFromDatabase() {
        try {
            Thread.sleep(15); // 模拟复杂查询延迟
            List<String> users = new ArrayList<>();
            userDatabase.forEach((id, info) -> {
                users.add(id + ": " + info.get("name"));
            });
            
            // 如果没有用户，添加一些模拟数据
            if (users.isEmpty()) {
                users.add("default_user_1: Default User 1");
                users.add("default_user_2: Default User 2");
            }
            
            return users;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }
    
    // 模拟保存到数据库
    @Trace
    @Tags({@Tag(key = "id", value = "arg[0]"), @Tag(key = "data", value = "arg[1]")})
    private void saveToDatabase(String id, Map<String, Object> data) {
        try {
            Thread.sleep(12); // 模拟数据库操作延迟
            userDatabase.put(id, new HashMap<>(data));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // 模拟更新数据库
    @Trace
    @Tags({@Tag(key = "id", value = "arg[0]"), @Tag(key = "data", value = "arg[1]")})
    private void updateDatabase(String id, Map<String, Object> data) {
        try {
            Thread.sleep(10); // 模拟数据库操作延迟
            if (userDatabase.containsKey(id)) {
                userDatabase.get(id).putAll(data);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}