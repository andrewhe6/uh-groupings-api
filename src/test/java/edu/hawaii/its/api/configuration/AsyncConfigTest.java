package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig();

    @Test
    public void construction() {
        assertNotNull(asyncConfig);
    }

    @Test
    public void getAsyncExecutorTest() {
        assertNotNull(asyncConfig.getAsyncExecutor());
    }

    @Test
    public void runsTenJobsAtOnceBeforeQueueingAny() throws InterruptedException {
        // An import holds its thread for the whole import, so a job must not queue while threads could be started.
        ThreadPoolTaskExecutor threadPool = asyncConfig.threadPool();
        CountDownLatch started = new CountDownLatch(10);
        CountDownLatch finish = new CountDownLatch(1);
        try {
            for (int i = 0; i < 10; i++) {
                threadPool.execute(() -> {
                    started.countDown();
                    try {
                        finish.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            assertTrue(started.await(5, TimeUnit.SECONDS), "Jobs still waiting to start: " + started.getCount());
            assertEquals(0, threadPool.getQueueSize());
            // Idle threads end, rather than staying for the life of the API.
            assertTrue(threadPool.getThreadPoolExecutor().allowsCoreThreadTimeOut());
        } finally {
            finish.countDown();
            threadPool.shutdown();
        }
    }
}
