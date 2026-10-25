package com.example.transactionalsystemjavareactnative.runtime;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Wires the runtime kernel into Spring: a pooled DataSource from DATABASE_URL (libpq URL, shared with
 * JPA), the CommandService, and the outbox relay below.
 */
@Configuration
public class RuntimeConfiguration {
    @Bean
    public DataSource dataSource() {
        String url = System.getenv("DATABASE_URL");
        if (url == null || url.isBlank()) throw new IllegalStateException("DATABASE_URL is not set (postgres://user:password@host:5432/db)");
        URI uri = URI.create(url.replaceFirst("^postgres(ql)?://", "postgresql://"));
        String[] userInfo = uri.getRawUserInfo() == null ? new String[0] : uri.getRawUserInfo().split(":", 2);
        return DataSourceBuilder.create()
            .url(ConnectionFactory.jdbcUrl(uri))
            .username(userInfo.length > 0 ? URLDecoder.decode(userInfo[0], StandardCharsets.UTF_8) : null)
            .password(userInfo.length > 1 ? URLDecoder.decode(userInfo[1], StandardCharsets.UTF_8) : null)
            .build();
    }

    /**
     * The kernel's tables are created before anything uses this factory (the command service, the relay):
     * idempotent and serialized across replicas. GHK_MIGRATE=false when a migration job or another tool owns
     * the schema (and this service connects without DDL rights).
     */
    @Bean
    public ConnectionFactory connectionFactory(DataSource dataSource, @Value("${ghk.schema.migrate:true}") boolean migrate) throws java.sql.SQLException {
        ConnectionFactory connections = dataSource::getConnection;
        if (migrate) Schema.migrate(connections, "public");
        return connections;
    }

    @Bean
    public CommandService commandService(ConnectionFactory connections) {
        return new CommandService(connections, "public");
    }

    /** Publishes the outbox every 1000 ms when a broker is configured (Broker: GHK_BROKER, AMQP_URL, PUBSUB_*). */
    @Component
    public static class OutboxRelayWorker implements DisposableBean {
        private static final Logger LOG = LoggerFactory.getLogger(OutboxRelayWorker.class);
        private final ConnectionFactory connections;
        private final boolean enabled;
        private final String workerId = "transaccion-reactiva-con-spring-web-flux-y-react-native-" + UUID.randomUUID();
        private Broker broker;
        private EventPublisher publisher;
        private OutboxRelay relay;
        private boolean warned;

        public OutboxRelayWorker(ConnectionFactory connections, @Value("${ghk.outbox.relay-enabled:true}") boolean enabled) {
            this.connections = connections;
            this.enabled = enabled;
        }

        @Scheduled(fixedDelay = 1000)
        public synchronized void publish() {
            if (!enabled) return;
            if (!Broker.configured()) {
                if (!warned) LOG.warn("No event broker is configured (AMQP_URL, or GHK_BROKER=pubsub with PUBSUB_PROJECT_ID): outbox events are stored but not published.");
                warned = true;
                return;
            }
            try {
                if (relay == null) {
                    broker = Broker.fromEnv();
                    // GHK_DECLARE_TOPOLOGY=false when infrastructure as code owns the topology (least-privilege credentials).
                    if (!"false".equals(System.getenv("GHK_DECLARE_TOPOLOGY"))) broker.declare(Topology.DEFAULT);
                    publisher = broker.publisher(Topology.DEFAULT.exchange());
                    relay = new OutboxRelay(connections, publisher, "public");
                }
                relay.publishBatch(workerId, 50);
            } catch (Exception e) {
                LOG.error("Outbox relay failed: {}", e.getMessage());
                close();
            }
        }

        private void close() {
            relay = null;
            try { if (publisher != null) publisher.close(); } catch (Exception ignored) { }
            try { if (broker != null) broker.close(); } catch (Exception ignored) { }
            publisher = null;
            broker = null;
        }

        @Override
        public synchronized void destroy() {
            close();
        }
    }
}
