package com.wisewealth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = (auth != null && auth.getPrincipal() != null) ? auth.getPrincipal().toString() : "anonymous";

        log.info("Incoming request: {} {}{} principal={}", method, uri, (query != null ? "?" + query : ""), principal);

        try {
            log.debug("Starting request {} {} requestId={} principal={}", method, uri, requestId, principal);
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            long duration = System.currentTimeMillis() - start;
            if (status >= 500) {
                log.error("Request completed status={} timeMs={} method={} uri={} principal={} requestId={}", status, duration, method, uri, principal, requestId);
            } else if (status >= 400) {
                log.info("Request completed status={} timeMs={} method={} uri={} principal={} requestId={}", status, duration, method, uri, principal, requestId);
            } else {
                log.debug("Request completed status={} timeMs={} method={} uri={} principal={} requestId={}", status, duration, method, uri, principal, requestId);
            }
            MDC.remove("requestId");
        }
    }
}
