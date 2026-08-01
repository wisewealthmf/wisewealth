package com.wisewealth.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.regex.Pattern;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    /** Pattern that valid CORS origins must match — http(s)://hostname[:port] only. */
    private static final Pattern SAFE_ORIGIN =
            Pattern.compile("^https?://[a-zA-Z0-9._-]+(:[0-9]{1,5})?$");

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    /**
     * Allowed origin is externalised so it can be set per-environment without
     * changing code. Override with the CORS_ORIGIN environment variable in
     * staging / production.
     */
    @Value("${cors.allowed-origin:http://localhost:3000}")
    private String allowedOrigin;

    @Value("${cors.extra-origins:}")
    private List<String> extraOrigins;

    /**
     * Validate all configured CORS origins at startup.
     * Fails fast with a clear error if an invalid value is supplied via env var,
     * preventing a misconfigured origin from silently opening cross-origin access.
     */
    @PostConstruct
    void validateCorsOrigins() {
        List<String> all = new java.util.ArrayList<>();
        all.add(allowedOrigin);
        all.addAll(extraOrigins);
        for (String origin : all) {
            if (origin == null || origin.isBlank()) continue;
            if (!SAFE_ORIGIN.matcher(origin).matches()) {
                throw new IllegalStateException(
                    "Invalid CORS origin configured: '" + origin + "'. " +
                    "Origins must be in the form http(s)://hostname[:port]. " +
                    "Wildcards are not permitted.");
            }
        }
        log.info("CORS origins validated: {}", all);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF is intentionally disabled: the API is stateless (JWT via
                // Authorization header) and does not use cookie-based sessions.
                // If HttpOnly cookie auth is ever adopted, re-enable CSRF here.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/resend-verification").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/verify-email", "/auth/check-email").permitAll()
                        // Public lead-capture endpoints (POST only; GET/PUT are auth-gated — see M-3)
                        .requestMatchers(HttpMethod.POST, "/queries", "/consultations", "/free-guide",
                                "/wealth-check/leads", "/wealth-check/report").permitAll()
                        // Admin endpoints — ROLE_ADMIN required on every HTTP method
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        // All other endpoints require a valid JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, AnonymousAuthenticationFilter.class)
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = new java.util.ArrayList<>();
        origins.add(allowedOrigin);
        origins.addAll(extraOrigins);
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
