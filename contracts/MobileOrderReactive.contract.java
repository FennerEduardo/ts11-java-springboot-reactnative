package com.transactional.reactive.contract;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.math.BigDecimal;
import java.util.UUID;

public interface MobileOrderReactiveContract {

    record ReactiveOrderPayload(
        UUID orderId,
        UUID customerId,
        BigDecimal total,
        String currency,
        String idempotencyKey
    ) {}

    record OrderEventStreamResponse(
        UUID orderId,
        String status,
        long timestamp
    ) {}

    Mono<ReactiveOrderPayload> processOrderReactive(ReactiveOrderPayload payload);
    Flux<OrderEventStreamResponse> streamOrderStatus(UUID orderId);
}
