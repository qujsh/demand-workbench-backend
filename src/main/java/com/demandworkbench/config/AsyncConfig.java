package com.demandworkbench.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    @Bean("decompositionExecutor")
    ThreadPoolTaskExecutor decompositionExecutor(
            @Value("${decomposition.executor.core-pool-size}") int corePoolSize,
            @Value("${decomposition.executor.max-pool-size}") int maxPoolSize,
            @Value("${decomposition.executor.queue-capacity}") int queueCapacity) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("decompose-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        return executor;
    }
}
