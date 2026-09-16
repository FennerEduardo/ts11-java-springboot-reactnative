package com.example.app.infrastructure.outbox;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OutboxPublisher {
    private final OutboxRepository repository;
    private final IMessageBrokerPublisher messageBroker;

    public OutboxPublisher(OutboxRepository repository, IMessageBrokerPublisher messageBroker) {
        this.repository = repository;
        this.messageBroker = messageBroker;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        var page = org.springframework.data.domain.PageRequest.of(0, 50);
        List<OutboxMessage> pending = repository.findPendingForProcessing(page);

        for (OutboxMessage msg : pending) {
            try {
                messageBroker.publish(msg.getEventType(), msg.getPayload());
                msg.markAsPublished();
            } catch (Exception e) {
                msg.markAsFailed(e.getMessage());
            }
            repository.save(msg);
        }
    }
}
