package edu.hawaii.its.api.configuration;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final int THREADS = 10;

    @Override
    public Executor getAsyncExecutor() {
        return new DelegatingSecurityContextAsyncTaskExecutor(threadPool());
    }

    /**
     * The threads the @Async jobs run on. An import holds its thread for the whole import (about 25 minutes for
     * 10,000 members), so all THREADS threads are started before a job is queued: a ThreadPoolTaskExecutor starts
     * threads beyond its core pool size only once its queue is full. Idle threads end after a minute.
     */
    ThreadPoolTaskExecutor threadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(THREADS);
        executor.setMaxPoolSize(THREADS);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-thread-");
        executor.initialize();
        return executor;
    }
}
