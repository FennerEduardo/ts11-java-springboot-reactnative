package com.example.app.infrastructure.idempotency;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_keys", indexes = {
    @Index(name = "uk_idempotency_key", columnList = "idempotencyKey", unique = true)
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

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public IdempotencyKeyRecord() {}

    public IdempotencyKeyRecord(String idempotencyKey, String requestPath) {
        this.idempotencyKey = idempotencyKey;
        this.requestPath = requestPath;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public String getResponseBody() { return responseBody; }
    public int getResponseStatus() { return responseStatus; }

    public void setResponse(int status, String body) {
        this.responseStatus = status;
        this.responseBody = body;
    }
}
