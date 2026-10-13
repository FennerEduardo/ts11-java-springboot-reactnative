package com.example.transactionalsystemjavareactnative.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates /api requests with an HS256 bearer token (JWT_SECRET) and exposes the tenant from the
 * "tenant_id" claim as the request attribute "tenantId". The tenant is never read from a header such
 * as X-Tenant-Id, and there is no default tenant: requests without one get 401.
 */
@Component
@Order(1)
public class JwtTenantFilter extends OncePerRequestFilter {
    public static final String TENANT_CLAIM = "tenant_id";
    public static final String TENANT_ATTRIBUTE = "tenantId";
    private static final ObjectMapper JSON = new ObjectMapper();
    private final String secret;

    public JwtTenantFilter(@Value("${ghk.jwt-secret:}") String secret) {
        this.secret = secret;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String tenant = tenantOf(request.getHeader("Authorization"), secret);
        if (tenant == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"detail\":\"A valid bearer token with a tenant is required\"}");
            return;
        }
        request.setAttribute(TENANT_ATTRIBUTE, tenant);
        chain.doFilter(request, response);
    }

    /** Tenant claim of a valid HS256 token (signature, exp, nbf checked), else null. */
    public static String tenantOf(String authorization, String secret) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7) || secret == null || secret.isEmpty()) return null;
        String[] parts = authorization.substring(7).trim().split("\\.");
        if (parts.length != 3) return null;
        try {
            Map<?, ?> header = JSON.readValue(Base64.getUrlDecoder().decode(parts[0]), Map.class);
            if (!"HS256".equals(header.get("alg"))) return null;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
            if (!MessageDigest.isEqual(expected, Base64.getUrlDecoder().decode(parts[2]))) return null;
            Map<?, ?> claims = JSON.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
            long now = System.currentTimeMillis() / 1000;
            if (claims.get("exp") instanceof Number exp && now >= exp.longValue()) return null;
            if (claims.get("nbf") instanceof Number nbf && now < nbf.longValue()) return null;
            return claims.get(TENANT_CLAIM) instanceof String tenant && !tenant.isBlank() ? tenant : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Signs an HS256 token (tests and local tooling; production tokens come from your identity provider). */
    public static String sign(Map<String, Object> claims, String secret) throws Exception {
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
        String head = enc.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String body = enc.encodeToString(JSON.writeValueAsBytes(claims));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return head + "." + body + "." + enc.encodeToString(mac.doFinal((head + "." + body).getBytes(StandardCharsets.UTF_8)));
    }
}
