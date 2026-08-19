package com.example.backend.config;

import com.example.backend.auth.security.FirebaseAuthFilter;
import com.example.backend.auth.security.FirebaseTokenCache;
import com.example.backend.auth.security.RestAccessDeniedHandler;
import com.example.backend.auth.security.RestAuthenticationEntryPoint;
import com.example.backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseTokenCache tokenCache;
    private final AuthService authService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Nullable
    private final RateLimitFilter rateLimitFilter;

    @Value("${rate.limit.enabled:false}")
    private boolean rateLimitEnabled;

    @Bean
    public FirebaseAuthFilter firebaseAuthFilter() {
        return new FirebaseAuthFilter(tokenCache, authService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(firebaseAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        if (rateLimitEnabled && rateLimitFilter != null) {
            http.addFilterBefore(rateLimitFilter, FirebaseAuthFilter.class);
        }

        return http.build();
    }
}