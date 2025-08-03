package com.alibou.security.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Separate JPA Auditing Configuration
 * To avoid conflicts with auto-configuration
 */
@Configuration
//@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
}
