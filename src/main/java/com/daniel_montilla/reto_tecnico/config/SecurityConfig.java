package com.daniel_montilla.reto_tecnico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.daniel_montilla.reto_tecnico.filter.ApiKeyAuthFilter;
import com.daniel_montilla.reto_tecnico.filter.ApiLoggingFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final ApiKeyAuthFilter apiKeyAuthFilter;
  private final ApiLoggingFilter apiLoggingFilter;

  public SecurityConfig(ApiKeyAuthFilter apiKeyAuthFilter, ApiLoggingFilter apiLoggingFilter) {
    this.apiKeyAuthFilter = apiKeyAuthFilter;
    this.apiLoggingFilter = apiLoggingFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())

        .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(apiLoggingFilter, ApiKeyAuthFilter.class)

        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers("/ping", "/actuator/health").permitAll()
            .anyRequest().authenticated())

        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }
}