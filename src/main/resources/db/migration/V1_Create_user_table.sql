use grpc;
-- 创建用户表
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL COMMENT '用户姓名',
                       email VARCHAR(255) NOT NULL UNIQUE COMMENT '用户邮箱',
                       age INT DEFAULT 0 COMMENT '用户年龄',
                       phone VARCHAR(20) COMMENT '用户手机号',
                       status ENUM('ACTIVE', 'INACTIVE', 'DELETED') DEFAULT 'ACTIVE' COMMENT '用户状态',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                       version INT DEFAULT 0 COMMENT '乐观锁版本号',

                       INDEX idx_email (email),
                       INDEX idx_name (name),
                       INDEX idx_status (status),
                       INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 插入测试数据
INSERT INTO users (name, email, age, phone, status) VALUES
                                                        ('张三', 'zhangsan@example.com', 25, '13800138001', 'ACTIVE'),
                                                        ('李四', 'lisi@example.com', 30, '13800138002', 'ACTIVE'),
                                                        ('王五', 'wangwu@example.com', 28, '13800138003', 'ACTIVE'),
                                                        ('赵六', 'zhaoliu@example.com', 35, '13800138004', 'INACTIVE');