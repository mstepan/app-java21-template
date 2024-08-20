package org.max.loom;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) throws Exception {

        // start 1M of virtual threads
        final AtomicLong counter = new AtomicLong(0L);
        final int threadsCount = 1_000_000;

        CountDownLatch allStarted = new CountDownLatch(threadsCount);
        CountDownLatch allCompleted = new CountDownLatch(threadsCount);

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < threadsCount; ++i) {
                pool.execute(
                        () -> {
                            try {
                                allStarted.countDown();
                                allStarted.await();

                                for (int it = 0; it < 1000; ++it) {
                                    counter.incrementAndGet();
                                }
                            } catch (InterruptedException interEx) {
                                Thread.currentThread().interrupt();
                            } finally {
                                allCompleted.countDown();
                            }
                        });
            }
        }

        allCompleted.await();

        System.out.printf("counter: %d%n", counter.get());

        System.out.printf(
                "Main done... Java version used: %s\n", System.getProperty("java.version"));
    }
}
