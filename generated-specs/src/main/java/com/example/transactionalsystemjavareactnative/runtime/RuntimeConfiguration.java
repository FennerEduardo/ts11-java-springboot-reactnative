package com.example.transactionalsystemjavareactnative.runtime;

import com.rabbitmq.client.Channel;
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
            .url("jdbc:postgresql://" + uri.getHost() + ":" + (uri.getPort() > 0 ? uri.getPort() : 5432) + uri.getPath())
            .username(userInfo.length > 0 ? URLDecoder.decode(userInfo[0], StandardCharsets.UTF_8) : null)
            .password(userInfo.length > 1 ? URLDecoder.decode(userInfo[1], StandardCharsets.UTF_8) : null)
            .build();
    }

    @Bean
    public ConnectionFactory connectionFactory(DataSource dataSource) {
        return dataSource::getConnection;
    }

    @Bean
    public CommandService commandService(ConnectionFactory connections) {
        return new CommandService(connections, "public");
    }

    /** Publishes the outbox every 1000 ms when AMQP_URL is set. */
    @Component
    public static class OutboxRelayWorker implements DisposableBean {
        private static final Logger LOG = LoggerFactory.getLogger(OutboxRelayWorker.class);
        private final ConnectionFactory connections;
        private final boolean enabled;
        private final String workerId = "transaccion-reactiva-con-spring-web-flux-y-react-native-" + UUID.randomUUID();
        private com.rabbitmq.client.Connection amqp;
        private OutboxRelay relay;
        private boolean warned;

        public OutboxRelayWorker(ConnectionFactory connections, @Value("${ghk.outbox.relay-enabled:true}") boolean enabled) {
            this.connections = connections;
            this.enabled = enabled;
        }

        @Scheduled(fixedDelay = 1000)
        public synchronized void publish() {
            if (!enabled) return;
            String url = System.getenv("AMQP_URL");
            if (url == null || url.isBlank()) {
                if (!warned) LOG.warn("AMQP_URL is not set: outbox events are stored but not published.");
                warned = true;
                return;
            }
            try {
                if (relay == null) {
                    var factory = new com.rabbitmq.client.ConnectionFactory();
                    factory.setUri(url);
                    amqp = factory.newConnection();
                    Channel channel = amqp.createChannel();
                    Topology.DEFAULT.declare(channel);
                    relay = new OutboxRelay(connections, channel, Topology.DEFAULT.exchange(), "public");
                }
                relay.publishBatch(workerId, 50);
            } catch (Exception e) {
                LOG.error("Outbox relay failed: {}", e.getMessage());
                close();
            }
        }

        private void close() {
            relay = null;
            try { if (amqp != null) amqp.close(); } catch (Exception ignored) { }
            amqp = null;
        }

        @Override
        public synchronized void destroy() {
            close();
        }
    }
}
