package org.example.bankcards.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankcards.config.EndpointAccess.ApiAccessConfig;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiAccessConfig apiAccessConfig;

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    apiAccessConfig.getPermittedPaths().stream()
                            .peek(e -> log.info("Permitted url: {}", e))
                            .forEach(e -> auth.requestMatchers(e.path()).permitAll());
                    apiAccessConfig.getAuthenticatedPaths().stream()
                            .peek(e -> log.debug("Authenticated url: {}; roles: {}",
                                    e.path(), e.roles()))
                            .forEach(e -> auth.requestMatchers(e.path())
                                    .hasAnyRole(getRolesArray(e.roles())));
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(
                        sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    private String[] getRolesArray(List<RoleType> roles) {
        return roles.stream()
                .map(r -> r.value)
                .toArray(String[]::new);
    }

//    private List<String> getPermittedPaths() {
//        return Stream.concat(apiPermitAccessConfig.getPermittedPaths().stream(),
//                apiPermitAccessConfig.getStaffPaths().stream())
//                .map(ApiPermitAccessConfig.Endpoint::path)
//                .toList();
//    }
}
