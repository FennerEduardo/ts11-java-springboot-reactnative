package com.example.transactionalsystemjavareactnative.infrastructure.outbox;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages", indexes = {
    @Index(name = "idx_outbox_status_occurred", columnList = "status, occurredOn")
})
public class OutboxMessage {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant occurredOn = Instant.now();

    private Instant processedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.PENDING;

    private int retryCount = 0;
    private String error;

    public enum OutboxStatus { PENDING, PROCESSING, PUBLISHED, FAILED }

    public OutboxMessage() {}
    public OutboxMessage(String eventType, String payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Instant getOccurredOn() { return occurredOn; }
    public Instant getProcessedOn() { return processedOn; }
    public OutboxStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public String getError() { return error; }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.processedOn = Instant.now();
    }

    public void markAsFailed(String error) {
        this.retryCount++;
        this.error = error;
        if (this.retryCount >= 5) {
            this.status = OutboxStatus.FAILED;
        } else {
            this.status = OutboxStatus.PENDING;
        }
    }
}
