package org.example.bankcards.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.example.bankcards.config.EndpointAccess.ApiAccessConfig;
import org.example.bankcards.dto.AbstractUserDto;
import org.example.bankcards.security.service.JwtService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private final JwtService jwtService;
    private final ApiAccessConfig apiAccessConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getServletPath();
            if (isPathPermitted(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = extractToken(request);
            AbstractUserDto user = jwtService.getUserByToken(token);
            if (jwtService.verifyJwtToken(token, user)) {
                var auth = jwtService.getUsernamePasswordAuthToken(user);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                log.debug("JWT Token verification failed");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPathPermitted(String path) {
        return apiAccessConfig.getPermittedPaths().stream()
                .filter(e -> CollectionUtils.isEmpty(e.roles()))
                .map(e -> {
                    if (e.path().endsWith("/**")) {
                        return e.path().substring(0, e.path().length() - 3);
                    }
                    return e.path();
                })
                .anyMatch(path::startsWith);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}
