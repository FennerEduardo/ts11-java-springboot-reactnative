package com.example.transactionalsystemjavareactnative.runtime;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.HashMap;
import java.util.Map;

/** W3C trace-context helpers; spans go to the global OpenTelemetry (configure exporters with OTEL_*). */
public final class Telemetry {
    private static final W3CTraceContextPropagator PROPAGATOR = W3CTraceContextPropagator.getInstance();
    private static final TextMapGetter<Map<String, String>> GETTER = new TextMapGetter<>() {
        @Override public Iterable<String> keys(Map<String, String> carrier) { return carrier.keySet(); }
        @Override public String get(Map<String, String> carrier, String key) { return carrier == null ? null : carrier.get(key); }
    };

    private Telemetry() {}

    public static Tracer tracer() {
        return GlobalOpenTelemetry.getTracer("transaccion-reactiva-con-spring-web-flux-y-react-native");
    }

    /** Context whose parent is the span described by an incoming traceparent header. */
    public static Context contextFrom(String traceparent) {
        if (traceparent == null || traceparent.isEmpty()) return Context.current();
        return PROPAGATOR.extract(Context.current(), Map.of("traceparent", traceparent), GETTER);
    }

    /** traceparent header value for a span in a context (null when the span is invalid). */
    public static String traceparentOf(Context parent, Span span) {
        Map<String, String> carrier = new HashMap<>();
        PROPAGATOR.inject(parent.with(span), carrier, Map::put);
        return carrier.get("traceparent");
    }
}
