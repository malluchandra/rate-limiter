package com.example.ratelimiter.controller;

import com.example.ratelimiter.config.ClientRateLimit;
import com.example.ratelimiter.services.RateLimiterService;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/request")
    public ResponseEntity<Boolean> handleRequest(@RequestParam String clientId, @RequestParam(required = false) Long timestamp) {
        // Use current timestamp if not provided
        if (timestamp == null) {
            timestamp = Instant.now().toEpochMilli();
        }

        Optional<ClientRateLimit> clientConfigOpt = rateLimiterService.getClientConfig(clientId);

        if (clientConfigOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-RateLimit-Limit", "0")
                    .header("X-RateLimit-Remaining", "0")
                    .body(false);
        }

        ClientRateLimit clientConfig = clientConfigOpt.get();
        boolean isAllowed = rateLimiterService.isRequestAllowed(clientId, timestamp);
        long remainingRequests = rateLimiterService.getRemainingRequests(clientId, timestamp, clientConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Limit", String.valueOf(clientConfig.getMaxRequests()));
        headers.add("X-RateLimit-Remaining", String.valueOf(remainingRequests));

        if (isAllowed) {
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(true);
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .headers(headers)
                    .body(false);
        }
    }
}
