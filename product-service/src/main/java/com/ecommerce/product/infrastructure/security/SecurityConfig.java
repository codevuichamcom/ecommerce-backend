package com.ecommerce.product.infrastructure.security;

import com.ecommerce.common.security.Role;
import com.ecommerce.common.security.UserContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for Product Service.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Protected endpoints
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole(Role.ADMIN.name())

                        .anyRequest().authenticated())
                .addFilterBefore(new UserContextFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
