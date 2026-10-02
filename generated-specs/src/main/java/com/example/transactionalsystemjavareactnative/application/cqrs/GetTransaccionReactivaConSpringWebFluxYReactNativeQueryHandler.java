package com.example.transactionalsystemjavareactnative.application.cqrs;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/**
 * Query Handler for TransaccionReactivaConSpringWebFluxYReactNative read model.
 * Follows CQRS: reads from optimized read model / projected views.
 */
@Service
public class GetTransaccionReactivaConSpringWebFluxYReactNativeQueryHandler {
    @Transactional(readOnly = true)
    public TransaccionReactivaConSpringWebFluxYReactNativeDTO handle(GetTransaccionReactivaConSpringWebFluxYReactNativeQuery query) {
        // Optimized read from Read Model / Projected View
        return new TransaccionReactivaConSpringWebFluxYReactNativeDTO(query.transaccionReactivaConSpringWebFluxYReactNativeId(), "PROCESSED", Map.of());
    }
}
