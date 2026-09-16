package com.example.app.application.sagas;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saga_instances")
public class SagaInstance {
    @Id
    private UUID correlationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaState currentState;

    private UUID paymentId;
    private BigDecimal amount;
    private String failureReason;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public enum SagaState { STARTED, AUTHORIZED, COMPLETED, COMPENSATING, FAILED }

    public SagaInstance() {}

    public SagaInstance(UUID correlationId, UUID paymentId, BigDecimal amount) {
        this.correlationId = correlationId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currentState = SagaState.STARTED;
    }

    public UUID getCorrelationId() { return correlationId; }
    public SagaState getCurrentState() { return currentState; }
    public void setCurrentState(SagaState state) { 
        this.currentState = state; 
        this.updatedAt = Instant.now();
    }
    public UUID getPaymentId() { return paymentId; }
    public BigDecimal getAmount() { return amount; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String reason) { this.failureReason = reason; }
}
