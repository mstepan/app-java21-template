package org.max.loom;

import jdk.incubator.concurrent.StructuredTaskScope;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomStructuredTaskScopeMain {

    public static void main(String[] args) throws Exception {

        AtomicInteger counter = new AtomicInteger();

        final int threadsCount = 100;

        try (ShutdownOnValueInRange scope = new ShutdownOnValueInRange(70, 80)) {
            for (int i = 0; i < threadsCount; ++i) {
                final int id = i;
                scope.fork(
                        () -> {
                            counter.incrementAndGet();
                            return id;
                        });
            }

            scope.join();
            scope.throwIfFailed();

            System.out.printf("Value found: %d\n", scope.getValue());
        }

        System.out.printf("counter: %d\n", counter.get());

        System.out.printf(
                "CustomStructuredTaskScopeMain done... Java version used: %s\n",
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

        private volatile int value;
        private volatile Throwable ex;

        @Override
        protected void handleComplete(Future<Integer> future) {
            if (future.state() == Future.State.SUCCESS) {

                final int curValue = future.resultNow();

                if (curValue >= from && curValue <= to) {
                    value = curValue;
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

        public int getValue() {
            return value;
        }
    }
}
