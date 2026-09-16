package com.example.app.application.sagas;

import java.math.BigDecimal;
import java.util.UUID;

// Event and Command Records (Java 21)
public class PaymentSagaContract {
    public record PaymentInitiatedEvent(UUID correlationId, UUID paymentId, BigDecimal amount) {}
    public record PaymentAuthorizedEvent(UUID correlationId) {}
    public record PaymentFailedEvent(UUID correlationId, String reason) {}

    public record AuthorizePaymentCommand(UUID paymentId, BigDecimal amount) {}
    public record CapturePaymentCommand(UUID paymentId) {}
    public record CancelAuthorizationCommand(UUID paymentId, String reason) {}
}
