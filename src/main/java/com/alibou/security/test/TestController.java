package com.alibou.security.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple test controller to verify app is running
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/health")
    public String health() {
        return "✅ Application is running successfully! 🚀";
    }
    
    @GetMapping("/info")
    public Object info() {
        return new Object() {
            public final String status = "RUNNING";
            public final String message = "Spring Boot JWT Security App";
            public final String timestamp = java.time.LocalDateTime.now().toString();
            public final String database = "H2 In-Memory";
            public final String[] endpoints = {
                "/api/test/health",
                "/api/test/info", 
                "/api/v1/auth/register",
                "/api/v1/auth/authenticate",
                "/h2-console",
                "/swagger-ui/index.html"
            };
        };
    }
}
