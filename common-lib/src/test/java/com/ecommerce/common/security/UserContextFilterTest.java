package com.ecommerce.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserContextFilterTest {

    private UserContextFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new UserContextFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        UserContextHolder.clear();
        MDC.clear();
    }

    @Test
    void shouldPopulateContext_WhenHeadersArePresent() throws Exception {
        // Given
        when(request.getHeader(SecurityConstants.HEADER_USER_ID)).thenReturn("user123");
        when(request.getHeader(SecurityConstants.HEADER_USER_NAME)).thenReturn("testuser");
        when(request.getHeader(SecurityConstants.HEADER_USER_ROLES)).thenReturn("CUSTOMER,ADMIN");

        // When
        doAnswer(invocation -> {
            // Verify UserContextHolder inside the filter chain
            UserContext context = UserContextHolder.getContext();
            assertThat(context).isNotNull();
            assertThat(context.userId()).isEqualTo("user123");
            assertThat(context.username()).isEqualTo("testuser");
            assertThat(context.roles()).contains(Role.CUSTOMER, Role.ADMIN);

            // Verify Spring Security Context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getName()).isEqualTo("testuser");
            assertThat(auth.getAuthorities()).hasSize(2);

            // Verify MDC
            assertThat(MDC.get("userId")).isEqualTo("user123");
            assertThat(MDC.get("username")).isEqualTo("testuser");

            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        // Then
        // Verify chain continues
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldClearContext_AfterRequestIsProcessed() throws Exception {
        // Given
        when(request.getHeader(SecurityConstants.HEADER_USER_ID)).thenReturn("user123");

        // When
        filter.doFilterInternal(request, response, chain);

        // Then - Context should be cleared in finally block
        // Note: verify(chain).doFilter is called inside the try block, so context
        // should be active then.
        // But after method returns, it should be cleared.
        assertThat(UserContextHolder.getContext()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    void shouldNotPopulateContext_WhenHeadersAreMissing() throws Exception {
        // Given
        when(request.getHeader(SecurityConstants.HEADER_USER_ID)).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, chain);

        // Then
        assertThat(UserContextHolder.getContext()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }
}
