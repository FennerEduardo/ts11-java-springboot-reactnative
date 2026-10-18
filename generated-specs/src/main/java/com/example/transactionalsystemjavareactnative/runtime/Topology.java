package com.example.transactionalsystemjavareactnative.runtime;

/**
 * Event bus destinations: events are published to {@code exchange} (RabbitMQ topic exchange / Pub/Sub topic)
 * and consumed from {@code queue} (queue / pull subscription); dead-lettered messages go to {@code dlx}
 * and can be read from {@code dlq}.
 */
public record Topology(String exchange, String queue, String dlx, String dlq) {
    public static final Topology DEFAULT = new Topology("transactional-system-java-reactnative.events", "transactional-system-java-reactnative.consumer", "transactional-system-java-reactnative.dlx", "transactional-system-java-reactnative.dlq");

    public static Topology forPrefix(String prefix) {
        return new Topology(prefix + ".events", prefix + ".consumer", prefix + ".dlx", prefix + ".dlq");
    }
}
