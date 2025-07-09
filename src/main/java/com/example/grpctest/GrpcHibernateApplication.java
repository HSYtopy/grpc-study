package com.example.grpctest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * gRPC Hibernate 应用启动类 - 使用Lombok优化
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
@EnableCaching
@Slf4j
public class GrpcHibernateApplication {

    public static void main(String[] args) {
        log.info("启动 gRPC Hibernate 服务...");
        SpringApplication.run(GrpcHibernateApplication.class, args);
        log.info("gRPC Hibernate 服务启动完成!");
    }
}