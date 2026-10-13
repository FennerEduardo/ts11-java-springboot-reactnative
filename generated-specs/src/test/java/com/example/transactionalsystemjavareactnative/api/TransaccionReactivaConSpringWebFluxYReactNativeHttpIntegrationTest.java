package com.example.transactionalsystemjavareactnative.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.transactionalsystemjavareactnative.auth.JwtTenantFilter;
import com.example.transactionalsystemjavareactnative.runtime.ConnectionFactory;
import com.example.transactionalsystemjavareactnative.runtime.Schema;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** HTTP contract of the application layer against PostgreSQL (needs DATABASE_URL). */
@Tag("integration")
// The relay is off here: its publish spans would mix with the trace assertions of the runtime tests in this JVM.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"ghk.jwt-secret=" + TransaccionReactivaConSpringWebFluxYReactNativeHttpIntegrationTest.SECRET, "ghk.outbox.relay-enabled=false"})
class TransaccionReactivaConSpringWebFluxYReactNativeHttpIntegrationTest {
    static final String SECRET = "integration-test-secret-with-32-chars!";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @LocalServerPort
    int port;

    @BeforeAll
    static void migrate() throws Exception {
        Schema.migrate(ConnectionFactory.fromUrl(System.getenv("DATABASE_URL")), "public");
    }

    private HttpResponse<String> post(String path, Map<String, String> headers) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{}"));
        headers.forEach(req::header);
        return HTTP.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String bearer(Map<String, Object> claims) throws Exception {
        return "Bearer " + JwtTenantFilter.sign(claims, SECRET);
    }

    @Test
    void rejectsRequestsWithoutAValidTokenAndNeverTrustsXTenantId() throws Exception {
        String path = "/api/v1/transaccion-reactiva-con-spring-web-flux-y-react-native/" + UUID.randomUUID() + "/process_transaccion_reactiva_con_spring_web_flux_y_react_native";
        assertEquals(401, post(path, Map.of()).statusCode());
        assertEquals(401, post(path, Map.of("X-Tenant-Id", "acme")).statusCode());
        assertEquals(401, post(path, Map.of("Authorization", bearer(Map.of("sub", "u1")))).statusCode());
        assertEquals(401, post(path, Map.of("Authorization", "Bearer " + JwtTenantFilter.sign(Map.of(JwtTenantFilter.TENANT_CLAIM, "acme"), "wrong-secret"))).statusCode());
    }

    @Test
    void runsACommandIntoTheOutboxForTheTokenTenantIdempotently() throws Exception {
        String tenant = "tenant-" + UUID.randomUUID();
        String id = UUID.randomUUID().toString();
        Map<String, String> headers = Map.of("Authorization", bearer(Map.of(JwtTenantFilter.TENANT_CLAIM, tenant)), "Idempotency-Key", UUID.randomUUID().toString(), "X-Tenant-Id", "someone-else");
        String path = "/api/v1/transaccion-reactiva-con-spring-web-flux-y-react-native/" + id + "/process_transaccion_reactiva_con_spring_web_flux_y_react_native";
        assertEquals(202, post(path, headers).statusCode());
        HttpResponse<String> replay = post(path, headers);
        assertEquals(200, replay.statusCode());
        assertTrue(replay.body().contains("replayed"));
        try (Connection conn = ConnectionFactory.fromUrl(System.getenv("DATABASE_URL")).open();
             PreparedStatement ps = conn.prepareStatement("SELECT tenant_id FROM ghk_outbox WHERE aggregate_id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(tenant, rs.getString(1));
            }
        }
    }

    @Test
    void mapsDomainValidationErrorsTo422() throws Exception {
        String path = "/api/v1/transaccion-reactiva-con-spring-web-flux-y-react-native/" + UUID.randomUUID() + "/no_such_command";
        assertEquals(422, post(path, Map.of("Authorization", bearer(Map.of(JwtTenantFilter.TENANT_CLAIM, "acme")))).statusCode());
    }
}
