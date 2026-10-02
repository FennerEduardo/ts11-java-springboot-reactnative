package com.example.transactionalsystemjavareactnative.application.cqrs;

import java.util.UUID;

/**
 * CQRS Commands for TransaccionReactivaConSpringWebFluxYReactNative domain.
 * Commands are immutable records (Java 21) that represent intent.
 */
public record CreateTransaccionReactivaConSpringWebFluxYReactNativeCommand(UUID tenantId, java.util.Map<String, Object> payload) {}
