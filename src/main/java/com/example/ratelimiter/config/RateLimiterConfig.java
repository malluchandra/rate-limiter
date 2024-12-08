package com.example.ratelimiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "rate.limiter")
public class RateLimiterConfig {

    private List<ClientRateLimit> clients;

    public List<ClientRateLimit> getClients() {
        return clients;
    }

    public void setClients(List<ClientRateLimit> clients) {
        this.clients = clients;
    }
}
