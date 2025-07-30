
package com.daniel_montilla.reto_tecnico.filter;

import com.daniel_montilla.reto_tecnico.entity.ApiLog;
import com.daniel_montilla.reto_tecnico.service.ApiLoggingService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

  @Autowired
  private ApiLoggingService loggingService;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    // Ignore logging for h2-console to avoid clutte
    if (request.getRequestURI().startsWith("/h2-console")) {
      filterChain.doFilter(request, response);
      return;
    }

    if (request.getRequestURI().startsWith("/api-logs")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Wrap request and response to cache their content
    ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

    // Proceed with the filter chain to execute the endpoint
    filterChain.doFilter(requestWrapper, responseWrapper);

    // Extract details after the endpoint has been processed
    String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
    String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

    loggingService.logRequest(ApiLog.builder()
        .httpMethod(request.getMethod())
        .endpointUrl(request.getRequestURI())
        .responseStatus(response.getStatus())
        .requestBody(requestBody)
        .responseBody(responseBody)
        .build());

    // Copy the cached response back to the original response
    responseWrapper.copyBodyToResponse();
  }
}