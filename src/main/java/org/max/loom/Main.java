package org.max.loom;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import jdk.incubator.concurrent.StructuredTaskScope;

public class Main {

    static class IntCounter {
        AtomicInteger value = new AtomicInteger(0);

        void inc() {
            value.incrementAndGet();
        }

        int getValue() {
            return value.get();
        }
    }

    public static void main(String[] args) throws Exception {

        IntCounter counter = new IntCounter();

        final int threadsCount = 100;

        try (ShutdownOnValueInRange scope = new ShutdownOnValueInRange(70, 80)) {
            for (int i = 0; i < threadsCount; ++i) {
                final int id = i;
                scope.fork(
                        () -> {
                            counter.inc();
                            return id;
                        });
            }

            scope.join();
            scope.throwIfFailed();

            System.out.printf("Price found: %d\n", scope.getPrice());
        }

        System.out.printf("counter: %d\n", counter.getValue());

        System.out.printf(
                "org.max.loom.Main done... Java version used: %s\n",
                System.getProperty("java.version"));
    }

    /**
     * Custom implementation of 'StructuredTaskScope'. org.max.loom.Main logic handled inside
     * 'handleComplete' method.
     */
    static class ShutdownOnValueInRange extends StructuredTaskScope<Integer> {

        final int from;
        final int to;

        public ShutdownOnValueInRange(int from, int to) {
            this.from = from;
            this.to = to;
        }

        private volatile int price;
        private volatile Throwable ex;

        @Override
        protected void handleComplete(Future<Integer> future) {
            if (future.state() == Future.State.SUCCESS) {

                final int curValue = future.resultNow();

                if (curValue >= from && curValue <= to) {
                    price = curValue;
                    shutdown();
                }
            } else if (future.state() == Future.State.FAILED) {
                this.ex = future.exceptionNow();
            }
        }

        public void throwIfFailed() throws Exception {
            if (ex != null) {
                throw new ExecutionException(ex);
            }
        }

        public int getPrice() {
            return price;
        }
    }

    private static long memoryUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
