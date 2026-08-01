package com.wisewealth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            log.debug("No Authorization header present for request {} {}", request.getMethod(), request.getRequestURI());
        } else if (jwtTokenProvider.validateToken(authorizationHeader)) {
            try {
                Long userId = jwtTokenProvider.extractUserId(authorizationHeader);
                boolean isAdmin = jwtTokenProvider.extractIsAdmin(authorizationHeader);

                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority(isAdmin ? "ROLE_ADMIN" : "ROLE_USER"));

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated request for userId={} admin={}", userId, isAdmin);
            } catch (Exception ex) {
                log.debug("Invalid token for request {} {} : {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
                // Invalid token — continue as anonymous
            }
        } else {
            log.debug("Authorization header present but token invalid for request {} {}", request.getMethod(), request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
