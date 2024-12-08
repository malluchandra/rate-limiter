package com.example.ratelimiter.services;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import com.example.ratelimiter.config.ClientRateLimit;
import com.example.ratelimiter.config.RateLimiterConfig;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class RateLimiterService {
    private static final Logger logger = Logger.getLogger(RateLimiterService.class.getName());
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimiterConfig rateLimiterConfig;
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.url:}")
    private String redisUrl;

    public RateLimiterService(RedisTemplate<String, String> redisTemplate, RateLimiterConfig rateLimiterConfig) {
        this.redisTemplate = redisTemplate;
        this.rateLimiterConfig = rateLimiterConfig;
    }

    public boolean isRequestAllowed(String clientId, long timestamp) {

        logger.info("Connecting to Redis...");
        logger.info("Redis Host: " + redisHost);
        logger.info("Redis Port: " + redisPort);
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
    public Optional<ClientRateLimit> getClientConfig(String clientId) {
        return rateLimiterConfig.getClients().stream()
                .filter(config -> config.getClientId().equals(clientId))
                .findFirst();
    }

    public long getRemainingRequests(String clientId, long timestamp, ClientRateLimit clientConfig) {
        String key = "rate_limiter:" + clientId;

        Long count = redisTemplate.opsForZSet().count(key, timestamp - clientConfig.getWindowSizeMs(), timestamp);
        if (count == null) {
            count = 0L;
        }

        return clientConfig.getMaxRequests() - count;
    }
}
