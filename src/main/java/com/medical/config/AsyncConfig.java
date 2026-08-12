package com.medical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import java.util.concurrent.Executor;

/**
 * Spring 线程池配置
 * @author wangda
 * @since 2026/08/13
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("medical-async-");
        executor.initialize();
        //Spring Security 提供的包装器包装线程池
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}