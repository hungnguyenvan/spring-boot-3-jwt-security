package com.alibou.security.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Configuration for the clean architecture
 * Enables custom repository implementation and auditing
 */
@Configuration
@EnableJpaRepositories(
    basePackages = {
        "com.alibou.security.book",
        "com.alibou.security.booktype", 
        "com.alibou.security.user",
        "com.alibou.security.token",
        "com.alibou.security.core",
        "com.alibou.security.document"
    }
)
public class JpaConfig {
}
