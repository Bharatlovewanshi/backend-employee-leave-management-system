
package com.leave.employee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import com.leave.employee.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Swagger access
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Registration and login
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()

                // Employee collection: Manager and Admin
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/employees"
                ).hasAnyRole("MANAGER", "ADMIN")

                // Own profile: authenticated users
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/employees/me"
                ).authenticated()

                // Employee lookup by ID: Manager and Admin
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/employees/*"
                ).hasAnyRole("MANAGER", "ADMIN")

                // Employee creation: Admin only
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/employees"
                ).hasRole("ADMIN")

             // Employee can update their own profile
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/employees/me"
                ).hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")

                // Admin can update any employee profile
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/employees/*"
                ).hasRole("ADMIN")

                // Employee deletion: Admin only
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/employees/**"
                ).hasRole("ADMIN")
                
                .requestMatchers(
	    HttpMethod.POST,
	    "/api/auth/users"
	).hasRole("ADMIN")
                
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
