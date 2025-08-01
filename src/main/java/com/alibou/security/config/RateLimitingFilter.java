package com.alibou.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static class RateLimitInfo {
        private LocalDateTime windowStart;
        private AtomicInteger requestCount;

        public RateLimitInfo() {
            this.windowStart = LocalDateTime.now();
            this.requestCount = new AtomicInteger(0);
        }

        public boolean allowRequest() {
            LocalDateTime now = LocalDateTime.now();
            
            // Reset counter nếu đã qua 1 phút
            if (now.isAfter(windowStart.plusMinutes(1))) {
                windowStart = now;
                requestCount.set(0);
            }

            // Cho phép tối đa 5 requests trong 1 phút
            return requestCount.incrementAndGet() <= 5;
        }
    }

    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Chỉ áp dụng rate limiting cho endpoint đăng ký
        if (request.getRequestURI().contains("/api/v1/auth/register")) {
            String clientIp = getClientIpAddress(request);
            RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(clientIp, k -> new RateLimitInfo());

            if (rateLimitInfo.allowRequest()) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many registration attempts. Please try again later.\",\"status\":429}");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null || xForwardedForHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xForwardedForHeader.split(",")[0].trim();
    }
}
