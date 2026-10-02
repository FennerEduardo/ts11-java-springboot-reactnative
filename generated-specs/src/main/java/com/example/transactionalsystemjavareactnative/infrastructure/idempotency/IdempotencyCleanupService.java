package com.example.transactionalsystemjavareactnative.infrastructure.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class IdempotencyCleanupService {
    private static final Logger log = LoggerFactory.getLogger(IdempotencyCleanupService.class);
    private final IdempotencyKeyRepository repository;

    public IdempotencyCleanupService(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredKeys() {
        // Keep keys for 24 hours
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        int deleted = repository.deleteOlderThan(cutoff);
        if (deleted > 0) {
            log.info("Cleaned up {} expired idempotency records.", deleted);
        }
    }
}
