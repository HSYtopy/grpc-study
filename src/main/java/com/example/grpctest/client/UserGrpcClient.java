package com.example.grpctest.client;

import com.example.grpctest.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * gRPC 测试客户端
 *
 * 主要功能：
 * - 通过gRPC协议远程调用UserService服务，实现用户的增删查改等操作
 * - 提供集成测试方法，方便通过HTTP接口或自动化脚本触发
 * - 作为Spring Bean自动管理生命周期
 */
@Component
@Slf4j
public class UserGrpcClient {

    private ManagedChannel channel; // gRPC通信通道
    private UserServiceGrpc.UserServiceBlockingStub blockingStub; // 阻塞式存根（同步调用gRPC服务）

    @PostConstruct
    public void init() {
        // 初始化gRPC通道和存根
        this.channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext() // 明文传输，开发环境下方便调试
                .build();

        this.blockingStub = UserServiceGrpc.newBlockingStub(channel);
        log.info("gRPC 客户端初始化完成");
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        // Spring容器销毁前关闭gRPC通道，释放资源
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            log.info("gRPC 客户端已关闭");
        }
    }

    /**
     * 创建用户（调用服务端的createUser方法）
     */
    public void createUser(String name, String email, int age, String phone) {
        log.info("=== 创建用户测试 ===");
        log.info("创建用户: name={}, email={}, age={}, phone={}", name, email, age, phone);

        // 构造gRPC请求
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setName(name)
                .setEmail(email)
                .setAge(age)
                .setPhone(phone)
                .build();

        try {
            // 发起远程调用
            CreateUserResponse response = blockingStub.createUser(request);
            if (response.getSuccess()) {
                log.info("✅ 用户创建成功: {}", response.getUser());
                log.info("用户ID: {}, 姓名: {}, 邮箱: {}",
                        response.getUser().getId(),
                        response.getUser().getName(),
                        response.getUser().getEmail());
            } else {
                log.warn("❌ 用户创建失败: {}", response.getMessage());
            }
        } catch (StatusRuntimeException e) {
            log.error("❌ RPC 调用失败: {}", e.getStatus());
        }
    }

    /**
     * 获取用户（调用服务端的getUser方法）
     */
    public void getUser(long userId) {
        log.info("=== 获取用户测试 ===");
        log.info("获取用户: userId={}", userId);

        // 构造请求
        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId(userId)
                .build();

        try {
            // 调用服务
            GetUserResponse response = blockingStub.getUser(request);
            if (response.getSuccess()) {
                UserInfo user = response.getUser();
                log.info("✅ 获取用户成功:");
                log.info("  ID: {}", user.getId());
                log.info("  姓名: {}", user.getName());
                log.info("  邮箱: {}", user.getEmail());
                log.info("  年龄: {}", user.getAge());
                log.info("  电话: {}", user.getPhone());
                log.info("  状态: {}", user.getStatus());
                log.info("  创建时间: {}", new java.util.Date(user.getCreatedTime()));
            } else {
                log.warn("❌ 获取用户失败: {}", response.getMessage());
            }
        } catch (StatusRuntimeException e) {
            log.error("❌ RPC 调用失败: {}", e.getStatus());
        }
    }

    /**
     * 获取用户列表（流式返回，遍历所有用户）
     */
    public void listUsers(int page, int pageSize) {
        log.info("=== 获取用户列表测试 ===");
        log.info("获取用户列表: page={}, pageSize={}", page, pageSize);

        // 构造请求
        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPage(page)
                .setPageSize(pageSize)
                .build();

        try {
            // 调用服务，返回的是一个可遍历的响应流
            Iterator<UserResponse> responses = blockingStub.listUsers(request);
            int count = 0;
            log.info("📋 用户列表:");
            while (responses.hasNext()) {
                UserResponse response = responses.next();
                UserInfo user = response.getUser();
                count++;
                log.info("  {}. ID:{} 姓名:{} 邮箱:{} 年龄:{}",
                        count, user.getId(), user.getName(),
                        user.getEmail(), user.getAge());
            }
            log.info("✅ 用户列表获取完成，共 {} 个用户", count);
        } catch (StatusRuntimeException e) {
            log.error("❌ RPC 调用失败: {}", e.getStatus());
        }
    }

    /**
     * 更新用户信息
     */
    public void updateUser(long userId, String name, String email, int age, String phone) {
        log.info("=== 更新用户测试 ===");
        log.info("更新用户: userId={}, name={}, email={}, age={}, phone={}",
                userId, name, email, age, phone);

        // 构建请求，只设置非空参数
        UpdateUserRequest.Builder requestBuilder = UpdateUserRequest.newBuilder()
                .setUserId(userId);

        if (name != null && !name.trim().isEmpty()) {
            requestBuilder.setName(name);
        }
        if (email != null && !email.trim().isEmpty()) {
            requestBuilder.setEmail(email);
        }
        if (age > 0) {
            requestBuilder.setAge(age);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            requestBuilder.setPhone(phone);
        }

        try {
            UpdateUserResponse response = blockingStub.updateUser(requestBuilder.build());
            if (response.getSuccess()) {
                log.info("✅ 用户更新成功: {}", response.getUser());
            } else {
                log.warn("❌ 用户更新失败: {}", response.getMessage());
            }
        } catch (StatusRuntimeException e) {
            log.error("❌ RPC 调用失败: {}", e.getStatus());
        }
    }

    /**
     * 删除用户
     */
    public void deleteUser(long userId) {
        log.info("=== 删除用户测试 ===");
        log.info("删除用户: userId={}", userId);

        DeleteUserRequest request = DeleteUserRequest.newBuilder()
                .setUserId(userId)
                .build();

        try {
            DeleteUserResponse response = blockingStub.deleteUser(request);
            if (response.getSuccess()) {
                log.info("✅ 用户删除成功: {}", response.getMessage());
            } else {
                log.warn("❌ 用户删除失败: {}", response.getMessage());
            }
        } catch (StatusRuntimeException e) {
            log.error("❌ RPC 调用失败: {}", e.getStatus());
        }
    }

    /**
     * 一键执行所有gRPC相关测试，方便综合验证功能
     */
    public void runAllTests() {
        log.info("🚀 开始执行 gRPC 功能综合测试...");

        try {
            // 1. 获取ID为1的用户
            getUser(1L);
            Thread.sleep(1000);

            // 2. 创建新用户
            createUser("测试用户", "test@example.com", 25, "13800138888");
            Thread.sleep(1000);

            // 3. 测试重复邮箱
            createUser("重复邮箱", "test@example.com", 30, "13800138999");
            Thread.sleep(1000);

            // 4. 获取用户列表
            listUsers(1, 10);
            Thread.sleep(1000);

            // 5. 更新用户
            updateUser(1L, "张三丰", "zhangsan_new@example.com", 26, "13800138001");
            Thread.sleep(1000);

            // 6. 再查ID为1的用户
            getUser(1L);
            Thread.sleep(1000);

            // 7. 删除ID为3的用户
            deleteUser(3L);
            Thread.sleep(1000);

            // 8. 最终用户列表
            listUsers(1, 10);

            log.info("🎉 gRPC 功能综合测试完成!");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("测试被中断", e);
        }
    }
}