package com.pescayucatan.api_pesca_merida.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final long TIME_WINDOW_MS = 60_000;

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();

        if (shouldRateLimit(path)) {
            RateLimitBucket bucket = buckets.computeIfAbsent(path, k -> new RateLimitBucket());

            if (bucket.tryConsume()) {
                response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getRemaining()));
                response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
                chain.doFilter(request, response);
            } else {
                response.setStatus(429);
                response.setContentType("application/json");
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
                response.setHeader("Retry-After", "60");
                response.getWriter().write(
                    "{\"error\":\"Too Many Requests\",\"status\":429,\"message\":\"Rate limit exceeded. Try again in 1 minute.\"}"
                );
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean shouldRateLimit(String path) {
        return path.startsWith("/api/v1/ingestion") ||
               path.startsWith("/peces") ||
               path.equals("/actuator/health") ||
               path.equals("/actuator/info");
    }

    private static class RateLimitBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            long windowStartVal = windowStart.get();

            if (now - windowStartVal >= TIME_WINDOW_MS) {
                count.set(0);
                windowStart.set(now);
            }

            int current = count.get();
            if (current < MAX_REQUESTS_PER_MINUTE) {
                count.incrementAndGet();
                return true;
            }
            return false;
        }

        int getRemaining() {
            long now = System.currentTimeMillis();
            long windowStartVal = windowStart.get();

            if (now - windowStartVal >= TIME_WINDOW_MS) {
                return MAX_REQUESTS_PER_MINUTE;
            }
            return MAX_REQUESTS_PER_MINUTE - count.get();
        }
    }
}
