package com.example.transactionalsystemjavareactnative.infrastructure.idempotency;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_keys", indexes = {
    @Index(name = "uk_idempotency_key", columnList = "idempotencyKey", unique = true),
    @Index(name = "idx_idemp_status_created", columnList = "status, createdAt")
})
public class IdempotencyKeyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String requestPath;

    @Lob
    private String responseBody;

    private int responseStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status = IdempotencyStatus.PROCESSING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public enum IdempotencyStatus { PROCESSING, COMPLETED, FAILED }

    public IdempotencyKeyRecord() {}

    public IdempotencyKeyRecord(String idempotencyKey, String requestPath) {
        this.idempotencyKey = idempotencyKey;
        this.requestPath = requestPath;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public String getResponseBody() { return responseBody; }
    public int getResponseStatus() { return responseStatus; }
    public IdempotencyStatus getStatus() { return status; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setResponse(int status, String body, IdempotencyStatus state) {
        this.responseStatus = status;
        this.responseBody = body;
        this.status = state;
        this.updatedAt = Instant.now();
    }
    
    public void markProcessing() {
        this.status = IdempotencyStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }
}
