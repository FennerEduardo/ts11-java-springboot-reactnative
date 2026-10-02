package com.example.transactionalsystemjavareactnative.domain;

import java.time.Instant;
import java.util.Map;

public record TransaccionReactivaConSpringWebFluxYReactNativeDomainEvent(TransaccionReactivaConSpringWebFluxYReactNativeEventType type, String aggregateId, long version, Instant occurredOn, Map<String, Object> payload) {
}
