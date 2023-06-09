package org.max.loom;

import jdk.incubator.concurrent.ScopedValue;
import jdk.incubator.concurrent.StructuredTaskScope;

import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ExecutionException;

public class Main {

    private static class SimpleCounter {

        int value;

        private static final VarHandle VALUE_HANDLE;

        static {
            try {
                VALUE_HANDLE =
                        MethodHandles.lookup()
                                .findVarHandle(SimpleCounter.class, "value", int.class);
            } catch (ReflectiveOperationException ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        int incrementAndGet() {

            while (true) {
                int curValue = (int) VALUE_HANDLE.get(this);

                int nextValue = curValue + 1;

                if (VALUE_HANDLE.compareAndSet(this, curValue, nextValue)) {
                    return curValue;
                }
            }
        }

        int get() {
            return (int) VALUE_HANDLE.get(this);
        }
    }

    public static void main(String[] args) throws Exception {

        final SimpleCounter counter = new SimpleCounter();

        final int threadsCount = 10_000;

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (int i = 0; i < threadsCount; ++i) {
                scope.fork(counter::incrementAndGet);
            }
            scope.join();
            scope.throwIfFailed(IllegalStateException::new);
        }

        System.out.printf("counter: %d\n", counter.get());

        System.out.printf(
                "Main done... Java version used: %s\n", System.getProperty("java.version"));
    }

    private static long memoryUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
