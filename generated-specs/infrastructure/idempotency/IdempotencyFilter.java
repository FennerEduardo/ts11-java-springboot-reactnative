package com.example.app.infrastructure.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {
    private final IdempotencyKeyRepository repository;

    public IdempotencyFilter(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader("X-Idempotency-Key");

        if (key == null || key.isBlank() || !request.getMethod().matches("POST|PUT|PATCH")) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<IdempotencyKeyRecord> existing = repository.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            IdempotencyKeyRecord record = existing.get();
            response.setStatus(record.getResponseStatus());
            response.setContentType("application/json");
            response.getWriter().write(record.getResponseBody() != null ? record.getResponseBody() : "");
            return;
        }

        IdempotencyKeyRecord newRecord = new IdempotencyKeyRecord(key, request.getRequestURI());
        try {
            repository.save(newRecord);
        } catch (Exception e) {
            // Concurrency conflict: duplicate request being processed simultaneously
            response.setStatus(409);
            response.getWriter().write("{\"error\": \"Duplicate concurrent request in progress\"}");
            return;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        String responseBody = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
        newRecord.setResponse(responseWrapper.getStatus(), responseBody);
        repository.save(newRecord);

        responseWrapper.copyBodyToResponse();
    }
}
