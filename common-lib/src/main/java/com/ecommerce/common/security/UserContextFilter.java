package com.ecommerce.common.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servlet filter for extracting user context from request headers and
 * populating Spring Security Context.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public class UserContextFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserContextFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(SecurityConstants.HEADER_USER_ID);
        String username = request.getHeader(SecurityConstants.HEADER_USER_NAME);
        String rolesString = request.getHeader(SecurityConstants.HEADER_USER_ROLES);

        if (userId != null) {
            Set<Role> roles = Collections.emptySet();
            if (rolesString != null && !rolesString.isBlank()) {
                roles = Arrays.stream(rolesString.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Role::valueOf)
                        .collect(Collectors.toSet());
            }

            UserContext context = new UserContext(userId, username, roles);
            UserContextHolder.setContext(context);

            // Populate Spring Security Context
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

            logger.debug("UserContext set for user: {}, roles: {}", username, roles);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
