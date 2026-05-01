package com.ally.blogapp.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {

    // token string → expiry time; entries are cleaned up lazily on each lookup
    private final ConcurrentHashMap<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiry) {
        blacklist.put(token, expiry);
    }

    public boolean isBlacklisted(String token) {
        purgeExpired();
        return blacklist.containsKey(token);
    }

    // Remove tokens that have already expired — they can never be used again anyway
    private void purgeExpired() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
