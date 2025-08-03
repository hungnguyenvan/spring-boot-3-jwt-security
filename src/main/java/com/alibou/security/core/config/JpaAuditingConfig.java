package com.alibou.security.core.config;

import org.springframework.context.annotation.Configuration;

/**
 * Separate JPA Auditing Configuration
 * To avoid conflicts with auto-configuration
 */
@Configuration
//@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
}
