package com.example.ratelimiter;

import com.example.ratelimiter.config.ClientRateLimit;
import com.example.ratelimiter.config.RateLimiterConfig;
import com.example.ratelimiter.services.RateLimiterService;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class RateLimiterApplicationTests {

    private static GenericContainer<?> redisContainer;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private RateLimiterConfig rateLimiterConfig;

    @BeforeAll
    static void startRedisContainer() {
        redisContainer = new GenericContainer<>("redis:latest")
                .withExposedPorts(6379)
                .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                        cmd.getHostConfig().withPortBindings(
                                new PortBinding(Ports.Binding.bindPort(6379), new ExposedPort(6379))
                        )
                ));
        redisContainer.start();
        System.out.println("Redis is running at: " + redisContainer.getHost() + ":" + redisContainer.getMappedPort(6379));
        System.setProperty("spring.redis.host", redisContainer.getHost());
        System.setProperty("spring.redis.port", redisContainer.getMappedPort(6379).toString());
    }

    @AfterAll
    static void stopRedisContainer() {

        if (redisContainer != null) {
            redisContainer.stop();
        }
    }

    @BeforeEach
    void setupTestConfig() {
        ClientRateLimit client1 = new ClientRateLimit();
        client1.setClientId("client1");
        client1.setWindowSizeMs(10000); // 10 seconds
        client1.setMaxRequests(5);

        ClientRateLimit client2 = new ClientRateLimit();
        client2.setClientId("client2");
        client2.setWindowSizeMs(60000); // 1 minute
        client2.setMaxRequests(10);

        rateLimiterConfig.setClients(Arrays.asList(client1, client2));
    }

    @Test
    void testRequestAllowedWithinLimit() {
        // Arrange
        String clientId = "client1";
        long timestamp = System.currentTimeMillis();

        // Act
        redisTemplate.opsForZSet().add("rate_limiter:" + clientId, String.valueOf(timestamp), timestamp);
        boolean isAllowed = rateLimiterService.isRequestAllowed(clientId, timestamp);
        System.out.println(isAllowed);
        // Assert
        assertTrue(isAllowed);
    }

    @Test
    void testRequestDeniedExceedingLimit() {
        // Arrange
        String clientId = "client1";
        long timestamp = System.currentTimeMillis();

        // Add requests up to the limit
        for (int i = 0; i < 5; i++) {
            redisTemplate.opsForZSet().add("rate_limiter:" + clientId, String.valueOf(timestamp + i), timestamp + i);
        }

        // Act
        boolean isAllowed = rateLimiterService.isRequestAllowed(clientId, timestamp + 6);

        // Assert
        assertFalse(isAllowed);
    }

    @Test
    void testRemainingRequests() {
        // Arrange
        String clientId = "client1";
        long timestamp = System.currentTimeMillis();

        redisTemplate.opsForZSet().add("rate_limiter:" + clientId, String.valueOf(timestamp), timestamp);
        redisTemplate.opsForZSet().add("rate_limiter:" + clientId, String.valueOf(timestamp + 1), timestamp + 1);

        // Act
        Optional<ClientRateLimit> clientConfigOpt = rateLimiterService.getClientConfig(clientId);
        assertTrue(clientConfigOpt.isPresent());

        ClientRateLimit clientConfig = clientConfigOpt.get();
        long remainingRequests = rateLimiterService.getRemainingRequests(clientId, timestamp, clientConfig);

        // Assert
        assertEquals(3, remainingRequests); // 2 requests made, 3 remaining
    }

    @Test
    void testUnconfiguredClient() {
        // Arrange
        String clientId = "unknown-client";
        long timestamp = System.currentTimeMillis();

        // Act
        boolean isAllowed = rateLimiterService.isRequestAllowed(clientId, timestamp);

        // Assert
        assertFalse(isAllowed); // Deny requests for unconfigured client
    }

    @Test
    void testHeadersForRateLimit() {
        // Arrange
        String clientId = "client1";
        long timestamp = System.currentTimeMillis();

        redisTemplate.opsForZSet().add("rate_limiter:" + clientId, String.valueOf(timestamp), timestamp);

        // Act
        Optional<ClientRateLimit> clientConfigOpt = rateLimiterService.getClientConfig(clientId);
        assertTrue(clientConfigOpt.isPresent());

        ClientRateLimit clientConfig = clientConfigOpt.get();
        long remainingRequests = rateLimiterService.getRemainingRequests(clientId, timestamp, clientConfig);

        // Assert
        assertEquals(4, remainingRequests);
        assertEquals(5, clientConfig.getMaxRequests());
    }


}
