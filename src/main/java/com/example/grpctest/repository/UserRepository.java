package com.example.grpctest.repository;

import com.example.grpctest.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口 - Hibernate优化
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据邮箱查找用户（带缓存）
     */
    @Cacheable(value = "users", key = "#email")
    @QueryHints({
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
            @QueryHint(name = "org.hibernate.cacheMode", value = "NORMAL")
    })
    Optional<User> findByEmailAndStatusNot(String email, User.UserStatus status);

    /**
     * 根据邮箱查找用户（不包含已删除）
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status != 'DELETED'")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * 检查邮箱是否存在（排除指定ID和已删除）
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :id AND u.status != 'DELETED'")
    boolean existsByEmailAndIdNotAndStatusNot(@Param("email") String email,
                                              @Param("id") Long id);

    /**
     * 根据状态查找用户（带缓存）
     */
    @Cacheable(value = "usersByStatus", key = "#status")
    @QueryHints({
            @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<User> findByStatus(User.UserStatus status);

    /**
     * 根据状态分页查找用户
     */
    @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.createdAt DESC")
    Page<User> findByStatusOrderByCreatedAtDesc(@Param("status") User.UserStatus status,
                                                Pageable pageable);

    /**
     * 根据名称模糊查询（不包含已删除）
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.status != 'DELETED' ORDER BY u.createdAt DESC")
    List<User> findByNameContainingIgnoreCaseAndStatusNot(@Param("name") String name);

    /**
     * 根据年龄范围查询活跃用户
     */
    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge AND u.status = 'ACTIVE'")
    List<User> findActiveUsersByAgeBetween(@Param("minAge") Integer minAge,
                                           @Param("maxAge") Integer maxAge);

    /**
     * 查询活跃用户数量（带缓存）
     */
    @Cacheable(value = "userCount", key = "'active'")
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE'")
    long countActiveUsers();

    /**
     * 批量软删除用户
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 'DELETED' WHERE u.id IN :ids")
    int softDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID查找活跃用户
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.status != 'DELETED'")
    Optional<User> findActiveUserById(@Param("id") Long id);

    /**
     * 查找最近创建的用户
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.createdAt DESC")
    List<User> findRecentActiveUsers(Pageable pageable);

    /**
     * 使用原生SQL进行复杂查询
     */
    @Query(value = """
        SELECT u.* FROM users u 
        WHERE u.status = 'ACTIVE' 
        AND u.created_at >= DATE_SUB(NOW(), INTERVAL :days DAY)
        ORDER BY u.created_at DESC
        """, nativeQuery = true)
    List<User> findUsersCreatedInLastDays(@Param("days") int days);
}