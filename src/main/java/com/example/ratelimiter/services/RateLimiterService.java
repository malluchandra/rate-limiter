package com.example.ratelimiter.services;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import com.example.ratelimiter.config.ClientRateLimit;
import com.example.ratelimiter.config.RateLimiterConfig;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimiterConfig rateLimiterConfig;

    public RateLimiterService(RedisTemplate<String, String> redisTemplate, RateLimiterConfig rateLimiterConfig) {
        this.redisTemplate = redisTemplate;
        this.rateLimiterConfig = rateLimiterConfig;
    }

    public boolean isRequestAllowed(String clientId, long timestamp) {
        // Find the rate limit configuration for the client
        Optional<ClientRateLimit> clientConfigOpt = rateLimiterConfig.getClients().stream()
                .filter(config -> config.getClientId().equals(clientId))
                .findFirst();

        if (clientConfigOpt.isEmpty()) {
            // Default to deny if no configuration exists for the client
            return false;
        }

        ClientRateLimit clientConfig = clientConfigOpt.get();
        String key = "rate_limiter:" + clientId;

        // Count the number of requests in the current window
        Long count = redisTemplate.opsForZSet().count(key, timestamp - clientConfig.getWindowSizeMs(), timestamp);

        if (count != null && count < clientConfig.getMaxRequests()) {
            // Add the current timestamp to the sorted set
            redisTemplate.opsForZSet().add(key, String.valueOf(timestamp), timestamp);
            redisTemplate.expire(key, clientConfig.getWindowSizeMs(), TimeUnit.MILLISECONDS); // clear the redis for space
            return true;
        }

        return false;
    }
}
