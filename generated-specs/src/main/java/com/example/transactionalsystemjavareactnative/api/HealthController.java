package com.example.transactionalsystemjavareactnative.api;

import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Probes for container platforms: /health (process is up) and /health/ready (database reachable). */
@RestController
public class HealthController {
    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public Map<String, String> live() {
        return Map.of("status", "ok");
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, String>> ready() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            // reported below
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("status", "unavailable"));
    }
}
