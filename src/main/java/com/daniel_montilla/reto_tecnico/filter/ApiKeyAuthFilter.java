package com.daniel_montilla.reto_tecnico.filter;

import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

  private final ApiKeyAuthService apiKeyAuthService;

  public ApiKeyAuthFilter(ApiKeyAuthService apiKeyAuthService) {
    this.apiKeyAuthService = apiKeyAuthService;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String apiKey = authHeader.substring(7);

      if (apiKeyAuthService.isApiKeyValid(apiKey)) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            apiKey, null, new ArrayList<>());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    filterChain.doFilter(request, response);
  }
}