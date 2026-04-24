package com.ally.blogapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String POSTS = "posts";
    public static final String USERS = "users";
    public static final String TAGS = "tags";
    public static final String POPULAR_TAGS = "popularTags";
    public static final String TOP_AUTHORS = "topAuthors";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache(POSTS, 500, 5),
                buildCache(USERS, 500, 5),
                buildCache(TAGS, 200, 30),
                buildCache(POPULAR_TAGS, 50, 10),
                buildCache(TOP_AUTHORS, 50, 10)
        ));
        return manager;
    }

    private CaffeineCache buildCache(String name, long maxSize, long ttlMinutes) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .maximumSize(maxSize)
                        .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
    }
}
