package com.example.transactionalsystemjavareactnative.application.cqrs;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Command Handler for CreateTransaccionReactivaConSpringWebFluxYReactNative.
 * Follows CQRS: writes to the write model and publishes domain events.
 */
@Service
public class CreateTransaccionReactivaConSpringWebFluxYReactNativeCommandHandler {
    private final ApplicationEventPublisher eventPublisher;

    public CreateTransaccionReactivaConSpringWebFluxYReactNativeCommandHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UUID handle(CreateTransaccionReactivaConSpringWebFluxYReactNativeCommand command) {
        UUID transaccionReactivaConSpringWebFluxYReactNativeId = UUID.randomUUID();

        // 1. Save to Write Model (Domain DB)
        // repository.save(new TransaccionReactivaConSpringWebFluxYReactNative(transaccionReactivaConSpringWebFluxYReactNativeId, command.tenantId(), command.payload()));

        // 2. Publish Domain Event
        eventPublisher.publishEvent(new TransaccionReactivaConSpringWebFluxYReactNativeCreatedEvent(transaccionReactivaConSpringWebFluxYReactNativeId, command.tenantId()));
        return transaccionReactivaConSpringWebFluxYReactNativeId;
    }
}
