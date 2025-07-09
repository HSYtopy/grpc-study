package com.example.grpctest.grpc;

import com.example.grpctest.dto.UserDTO;
import com.example.grpctest.dto.UserMapper;
import com.example.grpctest.proto.*;
import com.example.grpctest.service.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * gRPC 用户服务 - 使用Lombok优化
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        log.info("gRPC 创建用户请求: {}", request.getEmail());

        try {
            // 请求验证
            validateCreateUserRequest(request);

            // 使用Builder模式创建DTO
            UserDTO userDTO = UserDTO.builder()
                    .name(request.getName().trim())
                    .email(request.getEmail().trim())
                    .age(request.getAge())
                    .phone(request.getPhone().trim())
                    .build();

            // 创建用户
            UserDTO createdUser = userService.createUser(userDTO);

            // 构建响应
            CreateUserResponse response = CreateUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("用户创建成功")
                    .setUser(userMapper.dtoToProto(createdUser))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC 用户创建成功: ID = {}", createdUser.getId());

        } catch (IllegalArgumentException e) {
            log.warn("gRPC 创建用户参数错误: {}", e.getMessage());
            sendErrorResponse(responseObserver, CreateUserResponse.class, e.getMessage());
        } catch (Exception e) {
            log.error("gRPC 创建用户时发生错误", e);
            sendErrorResponse(responseObserver, CreateUserResponse.class, "服务器内部错误");
        }
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        log.info("gRPC 获取用户请求: userId = {}", request.getUserId());

        try {
            Optional<UserDTO> userOpt = userService.findById(request.getUserId());

            if (userOpt.isEmpty()) {
                GetUserResponse response = GetUserResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("用户不存在")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            UserDTO user = userOpt.get();
            GetUserResponse response = GetUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("获取用户成功")
                    .setUser(userMapper.dtoToProto(user))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC 获取用户成功: ID = {}", user.getId());

        } catch (Exception e) {
            log.error("gRPC 获取用户时发生错误", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("服务器内部错误: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<UserResponse> responseObserver) {
        log.info("gRPC 获取用户列表请求: page = {}, pageSize = {}",
                request.getPage(), request.getPageSize());

        try {
            // 分页参数处理
            int page = Math.max(0, request.getPage() - 1); // 转换为0基索引
            int pageSize = request.getPageSize() > 0 ? Math.min(request.getPageSize(), 100) : 20;
            Pageable pageable = PageRequest.of(page, pageSize);

            Page<UserDTO> userPage = userService.findUsers(pageable);
            List<UserDTO> users = userPage.getContent();

            // 流式返回用户数据
            for (UserDTO user : users) {
                UserResponse userResponse = UserResponse.newBuilder()
                        .setUser(userMapper.dtoToProto(user))
                        .build();

                responseObserver.onNext(userResponse);

                // 模拟流式传输延迟
                Thread.sleep(10);
            }

            responseObserver.onCompleted();
            log.info("gRPC 用户列表返回完成，共 {} 个用户", users.size());

        } catch (Exception e) {
            log.error("gRPC 获取用户列表时发生错误", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("服务器内部错误: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> responseObserver) {
        log.info("gRPC 更新用户请求: userId = {}", request.getUserId());

        try {
            // 构建更新DTO（只设置非空字段）
            UserDTO.UserDTOBuilder builder = UserDTO.builder();

            if (!request.getName().trim().isEmpty()) {
                builder.name(request.getName().trim());
            }
            if (!request.getEmail().trim().isEmpty()) {
                builder.email(request.getEmail().trim());
            }
            if (request.getAge() > 0) {
                builder.age(request.getAge());
            }
            if (!request.getPhone().trim().isEmpty()) {
                builder.phone(request.getPhone().trim());
            }

            UserDTO updateDTO = builder.build();

            // 更新用户
            UserDTO updatedUser = userService.updateUser(request.getUserId(), updateDTO);

            UpdateUserResponse response = UpdateUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("用户更新成功")
                    .setUser(userMapper.dtoToProto(updatedUser))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC 用户更新成功: ID = {}", updatedUser.getId());

        } catch (IllegalArgumentException e) {
            log.warn("gRPC 更新用户参数错误: {}", e.getMessage());
            sendErrorResponse(responseObserver, UpdateUserResponse.class, e.getMessage());
        } catch (Exception e) {
            log.error("gRPC 更新用户时发生错误", e);
            sendErrorResponse(responseObserver, UpdateUserResponse.class, "服务器内部错误");
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        log.info("gRPC 删除用户请求: userId = {}", request.getUserId());

        try {
            boolean deleted = userService.deleteUser(request.getUserId());

            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                    .setSuccess(deleted)
                    .setMessage(deleted ? "用户删除成功" : "用户不存在")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC 用户删除结果: userId = {}, deleted = {}", request.getUserId(), deleted);

        } catch (Exception e) {
            log.error("gRPC 删除用户时发生错误", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("服务器内部错误: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    // 通用错误响应方法
    @SuppressWarnings("unchecked")
    private <T> void sendErrorResponse(StreamObserver<?> responseObserver, Class<T> responseClass, String message) {
        try {
            Object response = null;

            if (responseClass == CreateUserResponse.class) {
                response = CreateUserResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage(message)
                        .build();
            } else if (responseClass == UpdateUserResponse.class) {
                response = UpdateUserResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage(message)
                        .build();
            }

            if (response != null) {
                ((StreamObserver<Object>) responseObserver).onNext(response);
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            log.error("发送错误响应时发生异常", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(message)
                    .asRuntimeException());
        }
    }

    private void validateCreateUserRequest(CreateUserRequest request) {
        if (request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        if (request.getAge() < 0) {
            throw new IllegalArgumentException("年龄不能为负数");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}