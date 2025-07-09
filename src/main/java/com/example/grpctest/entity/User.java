package com.example.grpctest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类 - 使用Lombok优化
 *
 * 这是一个JPA实体类，对应数据库中的users表。它通过各种注解，实现了字段约束、自动记录创建/更新时间、
 * 乐观锁、软删除、二级缓存等常见功能，同时配合Lombok自动生成getter/setter等方法，极大简化了代码量。
 */
@Entity // 声明该类为JPA实体，对应数据库表
@Table(
        name = "users", // 指定表名为users
        indexes = {
                @Index(name = "idx_email", columnList = "email"), // email字段索引
                @Index(name = "idx_name", columnList = "name"), // name字段索引
                @Index(name = "idx_status", columnList = "status"), // status字段索引
                @Index(name = "idx_created_at", columnList = "createdAt") // createdAt字段索引
        }
)
@EntityListeners(AuditingEntityListener.class) // 启用Spring Data JPA审计，自动填充创建和更新时间
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE) // 开启Hibernate二级缓存，读写模式
@Data // Lombok注解，自动生成getter/setter、toString、equals、hashCode等
@Builder // Lombok注解，支持构建者模式
@NoArgsConstructor // Lombok注解，自动生成无参构造器
@AllArgsConstructor // Lombok注解，自动生成全参构造器
@FieldDefaults(level = AccessLevel.PRIVATE) // Lombok注解，所有字段默认private
@ToString(exclude = {"version"}) // toString时排除version字段，避免循环引用
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // 只用指定字段实现equals/hashcode
public class User implements Serializable {

    private static final long serialVersionUID = 1L; // 用于序列化

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    @EqualsAndHashCode.Include // 参与equals/hashCode
    Long id;

    @NotBlank(message = "用户名不能为空") // 校验非空
    @Column(name = "name", nullable = false, length = 100) // 数据库字段约束
    String name;

    @Email(message = "邮箱格式不正确") // 邮箱格式校验
    @NotBlank(message = "邮箱不能为空")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    @EqualsAndHashCode.Include // 参与equals/hashCode
    String email;

    @PositiveOrZero(message = "年龄不能为负数") // 年龄必须大于等于0
    @Column(name = "age")
    @Builder.Default
    Integer age = 0; // 默认年龄0

    @Column(name = "phone", length = 20) // 手机号
    String phone;

    @Enumerated(EnumType.STRING) // 枚举以字符串形式存储
    @Column(name = "status", length = 20)
    @Builder.Default
    UserStatus status = UserStatus.ACTIVE; // 默认状态：激活

    @CreationTimestamp // 自动填充创建时间
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp // 自动填充更新时间
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Version // 乐观锁版本号
    @Column(name = "version")
    @Builder.Default
    Integer version = 0;

    /**
     * 用户状态枚举
     * ACTIVE: 激活
     * INACTIVE: 非激活
     * DELETED: 已删除（软删除）
     */
    @Getter
    @AllArgsConstructor
    public enum UserStatus {
        ACTIVE("激活"),
        INACTIVE("非激活"),
        DELETED("已删除");

        private final String description; // 状态描述
    }

    /**
     * 软删除方法：只修改状态为DELETED，不实际删除数据库记录
     */
    public void softDelete() {
        this.status = UserStatus.DELETED;
    }

    /**
     * 激活用户：将状态修改为ACTIVE
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 检查是否为活跃用户
     * @return true表示激活状态
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    /**
     * 检查是否已删除
     * @return true表示已删除
     */
    public boolean isDeleted() {
        return UserStatus.DELETED.equals(this.status);
    }
}