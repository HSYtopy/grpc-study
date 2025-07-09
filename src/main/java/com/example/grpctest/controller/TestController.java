package com.example.grpctest.controller;

import com.example.grpctest.client.UserGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 测试控制器 - 提供HTTP接口来测试gRPC功能
 *
 * 作用：
 * - 作为REST风格的API，将HTTP请求转发给gRPC客户端，间接测试gRPC服务端功能
 * - 方便用Postman、curl等工具发起测试
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final UserGrpcClient grpcClient; // 注入gRPC客户端

    /**
     * 运行所有gRPC测试
     * POST /api/grpc/all
     */
    @PostMapping("/grpc/all")
    public Map<String, Object> runAllGrpcTests() {
        log.info("通过HTTP接口触发gRPC综合测试");

        try {
            grpcClient.runAllTests(); // 调用客户端方法
            return Map.of(
                    "success", true,
                    "message", "gRPC 综合测试执行完成，请查看日志"
            );
        } catch (Exception e) {
            log.error("gRPC测试执行失败", e);
            return Map.of(
                    "success", false,
                    "message", "gRPC测试执行失败: " + e.getMessage()
            );
        }
    }

    /**
     * 创建用户测试
     * POST /api/grpc/create-user
     * 请求体JSON: { "name": "xxx", "email": "xxx", "age": 20, "phone": "xxx" }
     */
    @PostMapping("/grpc/create-user")
    public Map<String, Object> testCreateUser(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String email = (String) request.get("email");
        Integer age = (Integer) request.get("age");
        String phone = (String) request.get("phone");

        try {
            grpcClient.createUser(name, email, age != null ? age : 0, phone);
            return Map.of("success", true, "message", "创建用户测试完成");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 获取用户测试
     * GET /api/grpc/get-user/{userId}
     */
    @GetMapping("/grpc/get-user/{userId}")
    public Map<String, Object> testGetUser(@PathVariable Long userId) {
        try {
            grpcClient.getUser(userId);
            return Map.of("success", true, "message", "获取用户测试完成");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 获取用户列表测试
     * GET /api/grpc/list-users?page=1&pageSize=10
     */
    @GetMapping("/grpc/list-users")
    public Map<String, Object> testListUsers(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        try {
            grpcClient.listUsers(page, pageSize);
            return Map.of("success", true, "message", "获取用户列表测试完成");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 健康检查
     * GET /api/health
     * 检查服务是否启动正常
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "service", "gRPC Hibernate Service"
        );
    }
}