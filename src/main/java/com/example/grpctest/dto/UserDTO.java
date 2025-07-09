package com.example.grpctest.dto;

import com.example.grpctest.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户数据传输对象（DTO） - 用于服务层/控制层/前后端数据传递
 *
 * 用途：
 * - 解耦实体类与接口返回/入参
 * - 提供参数校验、序列化等功能
 * - 避免暴露敏感字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化时忽略null字段
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    Long id; // 用户ID

    @NotBlank(message = "用户名不能为空")
    String name; // 用户名

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    String email; // 邮箱

    @PositiveOrZero(message = "年龄不能为负数")
    Integer age; // 年龄

    String phone; // 电话

    User.UserStatus status; // 用户状态（枚举）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt; // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt; // 更新时间

    Integer version; // 乐观锁版本号

    /**
     * 创建UserDTO的便捷静态方法
     */
    public static UserDTO of(String name, String email, Integer age, String phone) {
        return UserDTO.builder()
                .name(name)
                .email(email)
                .age(age)
                .phone(phone)
                .status(User.UserStatus.ACTIVE)
                .build();
    }
}