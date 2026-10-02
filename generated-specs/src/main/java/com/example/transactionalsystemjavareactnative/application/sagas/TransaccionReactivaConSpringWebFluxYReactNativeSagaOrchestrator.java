package com.example.transactionalsystemjavareactnative.application.sagas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TransaccionReactivaConSpringWebFluxYReactNative Saga Orchestrator.
 * Manages the lifecycle of the TransaccionReactivaConSpringWebFluxYReactNative saga through state transitions.
 * Each handler is transactional and idempotent.
 */
@Service
public class TransaccionReactivaConSpringWebFluxYReactNativeSagaOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(TransaccionReactivaConSpringWebFluxYReactNativeSagaOrchestrator.class);
    private final TransaccionReactivaConSpringWebFluxYReactNativeSagaInstanceRepository sagaRepository;
    private final ApplicationEventPublisher commandBus;

    public TransaccionReactivaConSpringWebFluxYReactNativeSagaOrchestrator(TransaccionReactivaConSpringWebFluxYReactNativeSagaInstanceRepository sagaRepository, ApplicationEventPublisher commandBus) {
        this.sagaRepository = sagaRepository;
        this.commandBus = commandBus;
    }

    @Transactional
    public void handle(TransaccionReactivaConSpringWebFluxYReactNativeSagaContract.TransaccionReactivaConSpringWebFluxYReactNativeInitiatedEvent event) {
        log.info("Saga initiated: correlationId={}", event.correlationId());
        var metadata = event.metadata() != null ? event.metadata().toString() : "{}";
        var saga = new TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance(event.correlationId(), event.transaccionReactivaConSpringWebFluxYReactNativeId(), metadata);
        sagaRepository.save(saga);

        // Commands are dispatched in-process; bridge them to your broker (e.g. via the outbox) as needed.
        commandBus.publishEvent(new TransaccionReactivaConSpringWebFluxYReactNativeSagaContract.AuthorizeTransaccionReactivaConSpringWebFluxYReactNativeCommand(event.transaccionReactivaConSpringWebFluxYReactNativeId(), event.metadata()));
    }

    @Transactional
    public void handle(TransaccionReactivaConSpringWebFluxYReactNativeSagaContract.TransaccionReactivaConSpringWebFluxYReactNativeAuthorizedEvent event) {
        log.info("Saga authorized: correlationId={}", event.correlationId());
        var saga = sagaRepository.findById(event.correlationId())
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + event.correlationId()));

        saga.transitionTo(TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance.SagaState.AUTHORIZED);
        saga.transitionTo(TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance.SagaState.COMPLETING);
        sagaRepository.save(saga);

        commandBus.publishEvent(new TransaccionReactivaConSpringWebFluxYReactNativeSagaContract.CompleteTransaccionReactivaConSpringWebFluxYReactNativeCommand(saga.getTransaccionReactivaConSpringWebFluxYReactNativeId()));
    }

    @Transactional
    public void handle(TransaccionReactivaConSpringWebFluxYReactNativeSagaContract.TransaccionReactivaConSpringWebFluxYReactNativeCompletedEvent event) {
        log.info("Saga completed: correlationId={}", event.correlationId());
        var saga = sagaRepository.findById(event.correlationId())
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + event.correlationId()));

        saga.transitionTo(TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance.SagaState.COMPLETED);
        sagaRepository.save(saga);
    }

    @Transactional
    public void handle(TransaccionReactivaConSpringWebFluxYReactNativeSagaContract.TransaccionReactivaConSpringWebFluxYReactNativeFailedEvent event) {
        log.info("Saga failed: correlationId={}, reason={}", event.correlationId(), event.reason());
        var saga = sagaRepository.findById(event.correlationId())
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + event.correlationId()));

        saga.transitionTo(TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance.SagaState.COMPENSATING);
        saga.setFailureReason(event.reason());

        commandBus.publishEvent(new TransaccionReactivaConSpringWebFluxYReactNativeSagaContract.CompensateTransaccionReactivaConSpringWebFluxYReactNativeCommand(saga.getTransaccionReactivaConSpringWebFluxYReactNativeId(), event.reason()));

        saga.transitionTo(TransaccionReactivaConSpringWebFluxYReactNativeSagaInstance.SagaState.FAILED);
        sagaRepository.save(saga);
    }
}
