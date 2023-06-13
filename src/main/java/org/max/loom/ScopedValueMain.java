package org.max.loom;

import jdk.incubator.concurrent.ScopedValue;
import jdk.incubator.concurrent.StructuredTaskScope;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/** Provides ScopedValue (https://openjdk.org/jeps/429) usage examples. */
public class ScopedValueMain {

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

    private static final ScopedValue<SimpleCounter> SCOPED_COUNTER = ScopedValue.newInstance();

    public static void main(String[] args) {

        final int threadsCount = 10_000;

        ScopedValue.where(SCOPED_COUNTER, new SimpleCounter())
                .run(
                        () -> {
                            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                                for (int i = 0; i < threadsCount; ++i) {
                                    scope.fork(SCOPED_COUNTER.get()::incrementAndGet);
                                }

                                try {
                                    scope.join();
                                    scope.throwIfFailed(IllegalStateException::new);
                                } catch (InterruptedException interEx) {
                                    Thread.currentThread().interrupt();
                                }
                            }

                            System.out.printf("scoped counter: %d\n", SCOPED_COUNTER.get().get());
                        });

        System.out.printf(
                "ScopedValueMain done... Java version used: %s\n",
                System.getProperty("java.version"));
    }

    private static long memoryUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
