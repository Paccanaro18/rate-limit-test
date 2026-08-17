package com.paccanaro.ratelimit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RateLimitResponse(
        @JsonProperty("allowed")
        boolean allowed,

        @JsonProperty("remaining")
        long remaining,

        @JsonProperty("reset_at")
        long resetAt,

        @JsonProperty("limite")
        int limite,

        @JsonProperty("window_seconds")
        int windowSeconds,

        @JsonProperty("retry_after")
        Long retryAfter
) {}
