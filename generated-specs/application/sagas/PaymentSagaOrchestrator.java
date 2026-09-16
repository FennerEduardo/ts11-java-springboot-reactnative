package com.example.app.application.sagas;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentSagaOrchestrator {
    private final SagaInstanceRepository sagaRepository;

    public PaymentSagaOrchestrator(SagaInstanceRepository sagaRepository) {
        this.sagaRepository = sagaRepository;
    }

    @Transactional
    public void handle(PaymentSagaContract.PaymentInitiatedEvent event) {
        SagaInstance saga = new SagaInstance(event.correlationId(), event.paymentId(), event.amount());
        sagaRepository.save(saga);

        // Dispatch Authorization Command
        // dispatch(new PaymentSagaContract.AuthorizePaymentCommand(saga.getPaymentId(), saga.getAmount()));
    }

    @Transactional
    public void handle(PaymentSagaContract.PaymentAuthorizedEvent event) {
        SagaInstance saga = sagaRepository.findById(event.correlationId())
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + event.correlationId()));

        saga.setCurrentState(SagaInstance.SagaState.AUTHORIZED);

        // Dispatch Capture
        // dispatch(new PaymentSagaContract.CapturePaymentCommand(saga.getPaymentId()));
        saga.setCurrentState(SagaInstance.SagaState.COMPLETED);
        sagaRepository.save(saga);
    }

    @Transactional
    public void handle(PaymentSagaContract.PaymentFailedEvent event) {
        SagaInstance saga = sagaRepository.findById(event.correlationId())
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + event.correlationId()));

        saga.setCurrentState(SagaInstance.SagaState.COMPENSATING);
        saga.setFailureReason(event.reason());

        // Execute Compensatory Action (Cancel Authorization)
        // dispatch(new PaymentSagaContract.CancelAuthorizationCommand(saga.getPaymentId(), event.reason()));

        saga.setCurrentState(SagaInstance.SagaState.FAILED);
        sagaRepository.save(saga);
    }
}
