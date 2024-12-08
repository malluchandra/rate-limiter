package com.example.ratelimiter.controller;
import com.example.ratelimiter.services.RateLimiterService;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;


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
        boolean isAllowed = rateLimiterService.isRequestAllowed(clientId, timestamp);

        if (isAllowed) {
            return ResponseEntity.ok(true); // 200 OK with true
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(false); // 429 Too Many Requests with false
        }
    }
}
