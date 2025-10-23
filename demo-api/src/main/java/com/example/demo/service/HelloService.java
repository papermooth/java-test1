package com.example.demo.service;

import java.util.List;
import java.util.Map;

public interface HelloService {
    String sayHello(String name);
    String getInfoById(String id);
    List<String> getUsers();
    Map<String, Object> createUser(String name, String email);
    boolean updateUser(String id, Map<String, Object> info);
}