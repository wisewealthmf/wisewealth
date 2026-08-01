package com.wisewealth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-process token-bucket rate limiter for sensitive endpoints.
 * <p>
 * Protected paths and their per-IP limits (requests / window):
 *   /auth/login              — 5  POST  per 60 s
 *   /auth/register           — 5  POST  per 60 s
 *   /auth/resend-verification— 5  POST  per 60 s
 *   /auth/check-email        — 20 GET   per 60 s  (email enumeration guard)
 *   /queries                 — 10 POST  per 60 s
 *   /consultations           — 10 POST  per 60 s
 *   /free-guide              — 5  POST  per 60 s
 *   /wealth-check/leads      — 10 POST  per 60 s
 *   /wealth-check/report     — 5  POST  per 60 s
 * <p>
 * X-Forwarded-For is only trusted when the request arrives from a configured
 * trusted proxy CIDR/IP (TRUSTED_PROXY env var). Otherwise getRemoteAddr() is
 * used directly, preventing IP-spoofing rate-limit bypasses.
 * <p>
 * Note: for multi-instance deployments replace this with Bucket4j + Redis.
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;

    private record LimitRule(int maxRequests, String method) {}

    private static final Map<String, LimitRule> RULES = Map.of(
            "/api/v1/auth/login",               new LimitRule(5,  "POST"),
            "/api/v1/auth/register",            new LimitRule(5,  "POST"),
            "/api/v1/auth/resend-verification", new LimitRule(5,  "POST"),
            "/api/v1/auth/check-email",         new LimitRule(20, "GET"),
            "/api/v1/queries",                  new LimitRule(10, "POST"),
            "/api/v1/consultations",            new LimitRule(10, "POST"),
            "/api/v1/free-guide",               new LimitRule(5,  "POST"),
            "/api/v1/wealth-check/leads",       new LimitRule(10, "POST"),
            "/api/v1/wealth-check/report",      new LimitRule(5,  "POST")
    );

    // Key: "path::ip", Value: [count, windowStartMs]
    private final Map<String, long[]> counters = new ConcurrentHashMap<>();

    /**
     * Comma-separated list of trusted proxy IPs/CIDRs.
     * Only when the request comes from one of these will X-Forwarded-For be trusted.
     * Set via TRUSTED_PROXY env var in production (e.g. the load-balancer IP).
     * Defaults to empty — X-Forwarded-For is ignored by default.
     */
    @Value("${security.trusted-proxy-ips:}")
    private Set<String> trustedProxyIps;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path   = request.getRequestURI();
        String method = request.getMethod();
        LimitRule rule = RULES.get(path);

        if (rule != null && rule.method().equalsIgnoreCase(method)) {
            String ip  = getClientIp(request);
            String key = path + "::" + ip;
            long   now = Instant.now().toEpochMilli();

            long[] slot = counters.compute(key, (k, v) -> {
                if (v == null || now - v[1] > WINDOW_MS) {
                    log.debug("Rate limit window reset for key={}", k);
                    return new long[]{1L, now};
                }
                v[0]++;
                return v;
            });

            log.debug("Rate limit check key={} count={} max={}", key, slot[0], rule.maxRequests());

            if (slot[0] > rule.maxRequests()) {
                log.warn("Rate limit exceeded for ip={} path={} count={}", ip, path, slot[0]);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Returns the real client IP.
     * X-Forwarded-For is only trusted when the direct connection (remoteAddr)
     * comes from a known trusted proxy — prevents spoofing via a fabricated header.
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (!trustedProxyIps.isEmpty() && trustedProxyIps.contains(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return remoteAddr;
    }
}
