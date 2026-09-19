package dev.jonclarke.pensionplanner.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jonclarke.pensionplanner.common.ErrorResponse;
import dev.jonclarke.pensionplanner.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token-bucket rate limiter for the authentication endpoints (register/login).
 * Each client IP (respecting X-Forwarded-For when behind a proxy) has a
 * private bucket, so a burst on registration cannot exhaust the login budget.
 * Applies per (method, path, IP) combination; when the bucket is empty the
 * request is rejected with 429 and a JSON error body.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final long STALE_BUCKET_NANOS = 15L * 60 * 1_000_000_000L;

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private long requestCounter;

    public RateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        return !uri.equals(LOGIN_PATH) && !uri.equals(REGISTER_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        TokenBucket bucket = buckets.computeIfAbsent(clientKey(request),
                key -> new TokenBucket(properties.capacity(), properties.refillPerMinute() / 60.0));
        if (bucket.tryAcquire()) {
            filterChain.doFilter(request, response);
        } else {
            reject(response);
        }
        sweepIfNeeded();
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader(FORWARDED_FOR_HEADER);
        String ip = forwarded != null && !forwarded.isBlank()
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
        return request.getMethod() + " " + request.getRequestURI() + " from " + ip;
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(
                Instant.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Too many requests; please try again later."));
    }

    private void sweepIfNeeded() {
        if (++requestCounter % 1024 != 0) {
            return;
        }
        long cutoff = System.nanoTime() - STALE_BUCKET_NANOS;
        buckets.entrySet().removeIf(entry -> entry.getValue().isStale(cutoff));
    }

    private static final class TokenBucket {
        private final double capacity;
        private final double tokensPerSecond;
        private double tokens;
        private long lastRefillNanos = System.nanoTime();
        private long lastAccessNanos = lastRefillNanos;

        TokenBucket(double capacity, double tokensPerSecond) {
            this.capacity = capacity;
            this.tokensPerSecond = tokensPerSecond;
            this.tokens = capacity;
        }

        synchronized boolean tryAcquire() {
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000d;
            tokens = Math.min(capacity, tokens + elapsedSeconds * tokensPerSecond);
            lastRefillNanos = now;
            lastAccessNanos = now;
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        synchronized boolean isStale(long cutoff) {
            return lastAccessNanos < cutoff;
        }
    }
}