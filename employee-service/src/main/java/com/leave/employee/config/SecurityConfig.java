
package com.leave.employee.config;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.leave.employee.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .cors(Customizer.withDefaults())

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

                    // Employee Leave Management APIs
                    .requestMatchers(
                            "/api/employees/me/leaves",
                            "/api/employees/me/leaves/**"
                    ).hasRole("EMPLOYEE")

                    // Swagger access
                    .requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**"
                    ).permitAll()

                    // Login and Registration
                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/auth/register",
                            "/api/auth/login"
                    ).permitAll()

                    // Get all employees
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/employees"
                    ).hasAnyRole("MANAGER", "ADMIN")

                    // Get logged-in employee profile
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/employees/me"
                    ).authenticated()

                    // Get employee by ID
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/employees/*"
                    ).hasAnyRole("MANAGER", "ADMIN")

                    // Create employee
                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/employees"
                    ).hasRole("ADMIN")

                    // Update own profile
                    .requestMatchers(
                            HttpMethod.PUT,
                            "/api/employees/me"
                    ).hasAnyRole("EMPLOYEE", "MANAGER", "ADMIN")

                    // Update employee by ID
                    .requestMatchers(
                            HttpMethod.PUT,
                            "/api/employees/*"
                    ).hasRole("ADMIN")

                    // Delete employee
                    .requestMatchers(
                            HttpMethod.DELETE,
                            "/api/employees/**"
                    ).hasRole("ADMIN")

                    // Create user
                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/auth/users"
                    ).hasRole("ADMIN")

                    .anyRequest().authenticated()
            )

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // Prevent duplicate servlet-container registration of JWT filter
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(jwtAuthenticationFilter);
        registration.setEnabled(false);

        return registration;
    }
}
