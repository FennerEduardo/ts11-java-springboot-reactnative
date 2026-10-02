package com.example.transactionalsystemjavareactnative.application.sagas;

import java.util.UUID;

/**
 * Saga Contract for TransaccionReactivaConSpringWebFluxYReactNative domain process.
 * Contains all events and commands involved in the saga orchestration.
 */
public class TransaccionReactivaConSpringWebFluxYReactNativeSagaContract {

    // === Events ===
    public record TransaccionReactivaConSpringWebFluxYReactNativeInitiatedEvent(UUID correlationId, UUID transaccionReactivaConSpringWebFluxYReactNativeId, java.util.Map<String, Object> metadata) {}
    public record TransaccionReactivaConSpringWebFluxYReactNativeAuthorizedEvent(UUID correlationId) {}
    public record TransaccionReactivaConSpringWebFluxYReactNativeCompletedEvent(UUID correlationId) {}
    public record TransaccionReactivaConSpringWebFluxYReactNativeFailedEvent(UUID correlationId, String reason) {}

    // === Commands ===
    public record AuthorizeTransaccionReactivaConSpringWebFluxYReactNativeCommand(UUID transaccionReactivaConSpringWebFluxYReactNativeId, java.util.Map<String, Object> metadata) {}
    public record CompleteTransaccionReactivaConSpringWebFluxYReactNativeCommand(UUID transaccionReactivaConSpringWebFluxYReactNativeId) {}
    public record CompensateTransaccionReactivaConSpringWebFluxYReactNativeCommand(UUID transaccionReactivaConSpringWebFluxYReactNativeId, String reason) {}
}
