package com.ally.blogapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

    public static final String ANALYTICS_EXECUTOR = "analyticsExecutor";
    public static final String NOTIFICATION_EXECUTOR = "notificationExecutor";

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    private static final int CORES = Runtime.getRuntime().availableProcessors();

    /**
     * Heavy-read pool — DB-bound work like author stats and trending aggregation.
     * Sized for I/O wait: core = CORES, max = CORES * 2, bounded queue so a
     * surge bounces to the caller-runs policy instead of exhausting heap.
     */
    @Bean(name = ANALYTICS_EXECUTOR)
    public Executor analyticsExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(CORES);
        exec.setMaxPoolSize(CORES * 2);
        exec.setQueueCapacity(100);
        exec.setKeepAliveSeconds(60);
        exec.setThreadNamePrefix("analytics-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        exec.setWaitForTasksToCompleteOnShutdown(true);
        exec.setAwaitTerminationSeconds(30);
        exec.initialize();
        return exec;
    }

    /**
     * Fire-and-forget pool — small notifications, audit events. Bursty,
     * short-lived, must never block a request thread. Larger queue, smaller
     * pool because individual tasks are cheap.
     */
    @Bean(name = NOTIFICATION_EXECUTOR)
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(Math.max(4, CORES));
        exec.setQueueCapacity(500);
        exec.setKeepAliveSeconds(30);
        exec.setThreadNamePrefix("notify-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        exec.setWaitForTasksToCompleteOnShutdown(true);
        exec.setAwaitTerminationSeconds(10);
        exec.initialize();
        return exec;
    }

    @Override
    public Executor getAsyncExecutor() {
        return analyticsExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Async failure in {} args={}", method.getName(), params, ex);
    }
}
