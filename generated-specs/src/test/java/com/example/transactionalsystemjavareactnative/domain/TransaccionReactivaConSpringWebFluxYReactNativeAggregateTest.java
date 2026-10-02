package com.example.transactionalsystemjavareactnative.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TransaccionReactivaConSpringWebFluxYReactNativeAggregateTest {

    @Test
    void startsInTheInitialStateWithNoEvents() {
        var aggregate = new TransaccionReactivaConSpringWebFluxYReactNativeAggregate("agg-1");
        assertEquals(TransaccionReactivaConSpringWebFluxYReactNativeState.PENDING, aggregate.getState());
        assertEquals(0, aggregate.getVersion());
        assertTrue(aggregate.getPendingEvents().isEmpty());
    }

    @Test
    void rejectsAnAggregateWithoutId() {
        assertThrows(DomainValidationException.class, () -> new TransaccionReactivaConSpringWebFluxYReactNativeAggregate(""));
    }

    @Test
    void processTransaccionReactivaConSpringWebFluxYReactNativeRecordsEmiteFlujoServerSentEventFluxConsumidoPorUseQueryEnReactNatiAndBumpsTheVersion() {
        var aggregate = new TransaccionReactivaConSpringWebFluxYReactNativeAggregate("agg-1");
        var event = aggregate.processTransaccionReactivaConSpringWebFluxYReactNative(new TransaccionReactivaConSpringWebFluxYReactNativeCommand("agg-1"));
        assertEquals(TransaccionReactivaConSpringWebFluxYReactNativeEventType.EmiteFlujoServerSentEventFluxConsumidoPorUseQueryEnReactNati, event.type());
        assertEquals(1, event.version());
        assertEquals(1, aggregate.getVersion());
        assertEquals(1, aggregate.getPendingEvents().size());
    }

    @Test
    void processTransaccionReactivaConSpringWebFluxYReactNativeRejectsACommandWithoutId() {
        var aggregate = new TransaccionReactivaConSpringWebFluxYReactNativeAggregate("agg-1");
        assertThrows(DomainValidationException.class, () -> aggregate.processTransaccionReactivaConSpringWebFluxYReactNative(new TransaccionReactivaConSpringWebFluxYReactNativeCommand("")));
        assertTrue(aggregate.getPendingEvents().isEmpty());
    }
}
