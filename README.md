# Sliding Window Rate Limiter

## Overview

This project implements a sliding window rate limiter using Redis. The limiter tracks requests per client and enforces configurable rate limits within a defined time window.

## Features

- Supports sliding window rate limiting with Redis.
- Configurable limits per client.
- Integration with Testcontainers for isolated testing.
- API headers for rate-limit metadata (e.g., `X-RateLimit-Limit`, `X-RateLimit-Remaining`).

## Running the Application

### Prerequisites

- Docker (for Redis or Testcontainers).
- Java 17+.
- Maven.

### Steps

1. Start Redis:
   ```bash
   docker run --name redis-test -d -p 6379:6379 redis
### Final Thoughts

The implementation  addresses the core requirements effectively, with  integration and a scalable design. Focus on:
- Enhancing logging and error handling.
- Adding tests for edge cases and concurrency.
- Improving documentation.
- Client config load from database or dynamic log from different source

Let me know if you'd like further help with any specific aspect!
------------------------------------------
mvn clean install
docker build -t mallucharan/rate-limiter:latest .

docker push mallucharan/rate-limiter:latest

docker-compose up