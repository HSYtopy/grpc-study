package com.example.grpctest.service.impl;

import com.example.grpctest.dto.UserDTO;
import com.example.grpctest.dto.UserMapper;
import com.example.grpctest.entity.User;
import com.example.grpctest.repository.UserRepository;
import com.example.grpctest.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类 - 使用Lombok优化
 * <p>提供用户相关的业务逻辑实现，包括创建、查询、更新、删除用户等功能，并集成缓存和事务管理。</p>
 */
@Service
@Transactional // 默认所有方法启用事务管理
@RequiredArgsConstructor // Lombok 自动注入 final 字段的构造函数
@Slf4j // Lombok 提供的日志注解，生成 SLF4J 日志对象
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository; // 用户数据访问层
    private final UserMapper userMapper; // 用户实体与 DTO 转换工具

    /**
     * 创建新用户
     * <p>将用户 DTO 转换为实体，检查邮箱唯一性，设置用户状态为 ACTIVE，并保存到数据库。</p>
     * @param userDTO 用户数据传输对象，包含用户信息
     * @return 保存后的用户 DTO
     * @throws IllegalArgumentException 如果邮箱已存在
     */
    @Override
    @CacheEvict(value = {"users", "usersByStatus", "userCount"}, allEntries = true) // 清除相关缓存
    public UserDTO createUser(UserDTO userDTO) {
        log.info("创建用户: {}", userDTO.getEmail()); // 记录创建用户日志

        // 检查邮箱是否已存在
        if (userRepository.findActiveUserByEmail(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("邮箱已存在: " + userDTO.getEmail());
        }

        // 将 DTO 转换为实体
        User user = userMapper.toEntity(userDTO);
        user.setStatus(User.UserStatus.ACTIVE); // 设置用户状态为活跃

        // 保存用户到数据库
        User savedUser = userRepository.save(user);
        UserDTO result = userMapper.toDTO(savedUser); // 转换为 DTO

        log.info("用户创建成功: ID = {}", savedUser.getId());
        return result;
    }

    /**
     * 根据 ID 查询用户
     * <p>从数据库中查找活跃用户，并将结果缓存以提高性能。</p>
     * @param id 用户 ID
     * @return 包含用户信息的 Optional<UserDTO>，若用户不存在则返回空 Optional
     */
    @Override
    @Transactional(readOnly = true) // 只读事务，优化性能
    @Cacheable(value = "users", key = "#id") // 缓存查询结果
    public Optional<UserDTO> findById(Long id) {
        log.debug("查找用户: ID = {}", id);

        // 查询活跃用户并转换为 DTO
        return userRepository.findActiveUserById(id)
                .map(userMapper::toDTO);
    }

    /**
     * 根据邮箱查询用户
     * <p>查找活跃用户并返回对应的 DTO。</p>
     * @param email 用户邮箱
     * @return 包含用户信息的 Optional<UserDTO>，若用户不存在则返回空 Optional
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByEmail(String email) {
        log.debug("根据邮箱查找用户: {}", email);

        // 查询活跃用户并转换为 DTO
        return userRepository.findActiveUserByEmail(email)
                .map(userMapper::toDTO);
    }

    /**
     * 查询所有活跃用户
     * <p>从数据库中获取状态为 ACTIVE 的用户列表，并缓存结果。</p>
     * @return 活跃用户 DTO 列表
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersByStatus", key = "'ACTIVE'") // 缓存活跃用户列表
    public List<UserDTO> findAllActiveUsers() {
        log.debug("查找所有活跃用户");

        // 查询活跃用户并转换为 DTO 列表
        List<User> users = userRepository.findByStatus(User.UserStatus.ACTIVE);
        return userMapper.toDTOList(users);
    }

    /**
     * 分页查询活跃用户
     * <p>根据分页参数查询活跃用户，并按创建时间降序排序。</p>
     * @param pageable 分页参数，包含页码和每页大小
     * @return 分页的用户 DTO 列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findUsers(Pageable pageable) {
        log.debug("分页查找用户: page = {}, size = {}", pageable.getPageNumber(), pageable.getPageSize());

        // 分页查询活跃用户并转换为 DTO
        Page<User> userPage = userRepository.findByStatusOrderByCreatedAtDesc(
                User.UserStatus.ACTIVE, pageable);
        return userPage.map(userMapper::toDTO);
    }

    /**
     * 更新用户信息
     * <p>根据 ID 查找用户，验证邮箱唯一性，更新用户信息并保存。</p>
     * @param id 用户 ID
     * @param userDTO 更新后的用户信息
     * @return 更新后的用户 DTO
     * @throws EntityNotFoundException 如果用户不存在
     * @throws IllegalArgumentException 如果邮箱已被其他用户使用
     */
    @Override
    @CacheEvict(value = {"users", "usersByStatus"}, key = "#id") // 清除相关缓存
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("更新用户: ID = {}", id);

        // 查找现有用户
        User existingUser = userRepository.findActiveUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));

        // 检查邮箱是否被其他用户使用
        if (userDTO.getEmail() != null &&
                !existingUser.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmailAndIdNotAndStatusNot(userDTO.getEmail(), id)) {
            throw new IllegalArgumentException("邮箱已被其他用户使用: " + userDTO.getEmail());
        }

        // 使用 MapStruct 更新实体
        userMapper.updateEntityFromDTO(userDTO, existingUser);

        // 保存更新后的用户
        User updatedUser = userRepository.save(existingUser);
        UserDTO result = userMapper.toDTO(updatedUser);

        log.info("用户更新成功: ID = {}", updatedUser.getId());
        return result;
    }

    /**
     * 删除用户（软删除）
     * <p>根据 ID 查找用户并执行软删除操作，标记用户状态为非活跃。</p>
     * @param id 用户 ID
     * @return 删除是否成功，成功返回 true，失败（用户不存在）返回 false
     */
    @Override
    @CacheEvict(value = {"users", "usersByStatus", "userCount"}, allEntries = true) // 清除相关缓存
    public boolean deleteUser(Long id) {
        log.info("删除用户: ID = {}", id);

        // 查找用户
        Optional<User> userOpt = userRepository.findActiveUserById(id);
        if (userOpt.isEmpty()) {
            log.warn("用户不存在或已删除: ID = {}", id);
            return false;
        }

        // 执行软删除
        User user = userOpt.get();
        user.softDelete(); // 调用实体方法标记为软删除
        userRepository.save(user);

        log.info("用户删除成功: ID = {}", id);
        return true;
    }

    /**
     * 检查邮箱是否已存在
     * <p>用于验证邮箱唯一性，可排除指定 ID 的用户。</p>
     * @param email 要检查的邮箱
     * @param excludeId 要排除的用户 ID（可为 null）
     * @return 邮箱是否存在，true 表示存在，false 表示不存在
     */
    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email, Long excludeId) {
        if (excludeId != null) {
            // 检查邮箱是否被其他非指定 ID 的用户使用
            return userRepository.existsByEmailAndIdNotAndStatusNot(email, excludeId);
        } else {
            // 检查邮箱是否存在于活跃用户中
            return userRepository.findActiveUserByEmail(email).isPresent();
        }
    }

    /**
     * 统计活跃用户数量
     * <p>查询数据库中状态为 ACTIVE 的用户总数，并缓存结果。</p>
     * @return 活跃用户数量
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userCount", key = "'active'") // 缓存活跃用户数量
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    /**
     * 查询最近注册的活跃用户
     * <p>根据限制数量查询最近注册的活跃用户，按创建时间排序。</p>
     * @param limit 返回的最大用户数量
     * @return 最近注册的用户 DTO 列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findRecentUsers(int limit) {
        log.debug("查找最近的 {} 个用户", limit);

        // 创建分页参数，限制返回数量
        Pageable pageable = PageRequest.of(0, limit);
        List<User> users = userRepository.findRecentActiveUsers(pageable);
        return userMapper.toDTOList(users);
    }

    /**
     * 根据名称搜索用户
     * <p>模糊查询用户名，忽略大小写，返回匹配的非删除用户列表。</p>
     * @param name 用户名关键字
     * @return 匹配的非删除用户 DTO 列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsersByName(String name) {
        log.debug("根据名称搜索用户: {}", name);

        // 模糊查询用户名，忽略大小写
        List<User> users = userRepository.findByNameContainingIgnoreCaseAndStatusNot(name);
        return userMapper.toDTOList(users);
    }
}