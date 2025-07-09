package com.example.grpctest.dto;

import com.example.grpctest.dto.UserDTO;
import com.example.grpctest.entity.User;
import com.example.grpctest.proto.UserInfo;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 用户映射接口 - 使用 MapStruct 实现实体与 DTO 以及 Proto 之间的转换
 * <p>提供 User 实体、UserDTO 和 UserInfo（Protobuf）之间的映射功能，
 * 支持单个对象、列表转换以及部分字段更新，集成 Spring 组件模型。</p>
 */
@Mapper(
        componentModel = "spring", // 集成 Spring，自动注册为 Spring Bean
        unmappedTargetPolicy = ReportingPolicy.IGNORE, // 忽略未映射的字段
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, // 忽略 null 值的属性映射
        builder = @Builder(disableBuilder = true) // 禁用 MapStruct 自动生成的 Builder
)
public interface UserMapper {

    // MapStruct 提供的静态工厂方法，用于获取 mapper 实例
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * 将 User 实体转换为 UserDTO
     * <p>映射所有字段，包括 version，确保 DTO 包含实体的完整信息。</p>
     * @param user 用户实体对象
     * @return 转换后的 UserDTO 对象
     */
    @Mapping(target = "version", source = "version") // 显式映射 version 字段
    UserDTO toDTO(User user);

    /**
     * 将 UserDTO 转换为 User 实体
     * <p>忽略 id、createdAt、updatedAt 和 version 字段，防止覆盖数据库中的关键字段。</p>
     * @param userDTO 用户 DTO 对象
     * @return 转换后的 User 实体对象
     */
    @Mapping(target = "id", ignore = true) // 忽略 ID，防止修改
    @Mapping(target = "createdAt", ignore = true) // 忽略创建时间
    @Mapping(target = "updatedAt", ignore = true) // 忽略更新时间
    @Mapping(target = "version", ignore = true) // 忽略版本号
    User toEntity(UserDTO userDTO);

    /**
     * 将 User 实体列表转换为 UserDTO 列表
     * <p>批量映射用户实体列表到 DTO 列表，保持字段一致性。</p>
     * @param users 用户实体列表
     * @return 转换后的 UserDTO 列表
     */
    List<UserDTO> toDTOList(List<User> users);

    /**
     * 从 UserDTO 更新 User 实体
     * <p>仅更新非 null 字段，忽略 id、createdAt、updatedAt 和 version 字段，保护关键字段不被修改。</p>
     * @param dto 用户 DTO 对象，包含更新的字段
     * @param entity 待更新的用户实体对象
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // 忽略 null 值字段
    @Mapping(target = "id", ignore = true) // 忽略 ID
    @Mapping(target = "createdAt", ignore = true) // 忽略创建时间
    @Mapping(target = "updatedAt", ignore = true) // 忽略更新时间
    @Mapping(target = "version", ignore = true) // 忽略版本号
    void updateEntityFromDTO(UserDTO dto, @MappingTarget User entity);

    /**
     * 将 User 实体转换为 Protobuf 的 UserInfo
     * <p>手动实现实体到 Protobuf 对象的转换，处理 null 值并将时间字段转换为 UTC 时间的毫秒值。</p>
     * @param user 用户实体对象
     * @return 转换后的 UserInfo Protobuf 对象，若输入为 null 则返回 null
     */
    default UserInfo toProto(User user) {
        if (user == null) return null; // 处理空输入
        // 使用 Builder 模式构建 UserInfo 对象
        return UserInfo.newBuilder()
                .setId(user.getId() == null ? 0L : user.getId()) // 设置 ID，null 时使用默认值 0
                .setName(user.getName() == null ? "" : user.getName()) // 设置名称，null 时使用空字符串
                .setEmail(user.getEmail() == null ? "" : user.getEmail()) // 设置邮箱，null 时使用空字符串
                .setAge(user.getAge() == null ? 0 : user.getAge()) // 设置年龄，null 时使用默认值 0
                .setPhone(user.getPhone() == null ? "" : user.getPhone()) // 设置电话，null 时使用空字符串
                .setCreatedTime(user.getCreatedAt() == null ? 0L : user.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli()) // 转换创建时间为 UTC 毫秒
                .setUpdatedTime(user.getUpdatedAt() == null ? 0L : user.getUpdatedAt().toInstant(ZoneOffset.UTC).toEpochMilli()) // 转换更新时间为 UTC 毫秒
                .setStatus(user.getStatus() == null ? "" : user.getStatus().name()) // 设置状态，null 时使用空字符串
                .build();
    }

    /**
     * 将 UserDTO 转换为 Protobuf 的 UserInfo
     * <p>手动实现 DTO 到 Protobuf 对象的转换，处理 null 值并将时间字段转换为 UTC 时间的毫秒值。</p>
     * @param userDTO 用户 DTO 对象
     * @return 转换后的 UserInfo Protobuf 对象，若输入为 null 则返回 null
     */
    default UserInfo dtoToProto(UserDTO userDTO) {
        if (userDTO == null) return null; // 处理空输入
        // 使用 Builder 模式构建 UserInfo 对象
        return UserInfo.newBuilder()
                .setId(userDTO.getId() == null ? 0L : userDTO.getId()) // 设置 ID，null 时使用默认值 0
                .setName(userDTO.getName() == null ? "" : userDTO.getName()) // 设置名称，null 时使用空字符串
                .setEmail(userDTO.getEmail() == null ? "" : userDTO.getEmail()) // 设置邮箱，null 时使用空字符串
                .setAge(userDTO.getAge() == null ? 0 : userDTO.getAge()) // 设置年龄，null 时使用默认值 0
                .setPhone(userDTO.getPhone() == null ? "" : userDTO.getPhone()) // 设置电话，null 时使用空字符串
                .setCreatedTime(userDTO.getCreatedAt() == null ? 0L : userDTO.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli()) // 转换创建时间为 UTC 毫秒
                .setUpdatedTime(userDTO.getUpdatedAt() == null ? 0L : userDTO.getUpdatedAt().toInstant(ZoneOffset.UTC).toEpochMilli()) // 转换更新时间为 UTC 毫秒
                .setStatus(userDTO.getStatus() == null ? "" : userDTO.getStatus().name()) // 设置状态，null 时使用空字符串
                .build();
    }

    /**
     * 将 LocalDateTime 转换为 UTC 时间戳（毫秒）
     * <p>用于时间字段的映射，转换为 Protobuf 所需的毫秒时间戳格式。</p>
     * @param localDateTime 输入的 LocalDateTime 对象
     * @return UTC 时间戳（毫秒），若输入为 null 则返回 0
     */
    @Named("localDateTimeToEpochMilli")
    default long localDateTimeToEpochMilli(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli() : 0L; // 转换时间为 UTC 毫秒，null 时返回 0
    }
}
