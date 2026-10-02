package com.example.transactionalsystemjavareactnative.domain;

import java.util.Map;

public record TransaccionReactivaConSpringWebFluxYReactNativeCommand(String id, Map<String, Object> payload) {
    public TransaccionReactivaConSpringWebFluxYReactNativeCommand(String id) {
        this(id, Map.of());
    }
}
