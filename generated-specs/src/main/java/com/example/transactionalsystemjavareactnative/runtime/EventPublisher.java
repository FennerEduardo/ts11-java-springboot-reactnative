package com.example.transactionalsystemjavareactnative.runtime;

/** Publishes to one destination; publish returns only once the broker has durably accepted the message. */
public interface EventPublisher extends AutoCloseable {
    String system();

    String destination();

    void publish(BrokerMessage message) throws Exception;
}
