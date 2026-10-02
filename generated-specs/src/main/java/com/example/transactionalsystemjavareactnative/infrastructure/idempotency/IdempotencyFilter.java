package com.example.transactionalsystemjavareactnative.infrastructure.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private final IdempotencyKeyRepository repository;
    private static final int PROCESSING_TTL_MINUTES = 2;

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

        Optional<IdempotencyKeyRecord> existingOpt = repository.findByIdempotencyKey(key);
        if (existingOpt.isPresent()) {
            IdempotencyKeyRecord existing = existingOpt.get();
            
            if (existing.getStatus() == IdempotencyKeyRecord.IdempotencyStatus.COMPLETED) {
                response.setStatus(existing.getResponseStatus());
                response.setContentType("application/json");
                response.getWriter().write(existing.getResponseBody() != null ? existing.getResponseBody() : "");
                return;
            }
            
            if (existing.getStatus() == IdempotencyKeyRecord.IdempotencyStatus.PROCESSING) {
                long ageMinutes = ChronoUnit.MINUTES.between(existing.getUpdatedAt(), Instant.now());
                if (ageMinutes < PROCESSING_TTL_MINUTES) {
                    response.setStatus(409); // Conflict
                    response.getWriter().write("{\"error\": \"Request already in progress. Retry after a moment.\"}");
                    return;
                } else {
                    log.warn("Idempotency key {} stuck in PROCESSING for {} mins. Allowing retry.", key, ageMinutes);
                    existing.markProcessing();
                    repository.save(existing);
                    // Proceed to process the retry
                }
            }
        }

        IdempotencyKeyRecord recordToUpdate;
        if (existingOpt.isEmpty()) {
            try {
                recordToUpdate = new IdempotencyKeyRecord(key, request.getRequestURI());
                repository.save(recordToUpdate);
            } catch (Exception e) {
                // Concurrency conflict: another thread inserted the key just now
                response.setStatus(409);
                response.getWriter().write("{\"error\": \"Duplicate concurrent request in progress\"}");
                return;
            }
        } else {
            recordToUpdate = existingOpt.get();
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, responseWrapper);
            
            String responseBody = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
            recordToUpdate.setResponse(responseWrapper.getStatus(), responseBody, IdempotencyKeyRecord.IdempotencyStatus.COMPLETED);
            repository.save(recordToUpdate);
        } catch (Exception e) {
            recordToUpdate.setResponse(500, "{\"error\":\"" + e.getMessage() + "\"}", IdempotencyKeyRecord.IdempotencyStatus.FAILED);
            repository.save(recordToUpdate);
            throw e;
        }

        responseWrapper.copyBodyToResponse();
    }
}
