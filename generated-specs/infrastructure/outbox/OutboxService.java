package com.example.app.infrastructure.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OutboxService {
    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public <T> void saveMessage(T domainEvent) {
        try {
            String payload = objectMapper.writeValueAsString(domainEvent);
            OutboxMessage message = new OutboxMessage(domainEvent.getClass().getSimpleName(), payload);
            repository.save(message);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing event for Outbox", e);
        }
    }
}
