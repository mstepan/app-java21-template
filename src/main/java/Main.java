import java.util.concurrent.Future;
import jdk.incubator.concurrent.StructuredTaskScope;

public class Main {

    static class IntCounter {
        int value;

        synchronized void inc() {
            ++value;
        }

        synchronized int getValue() {
            return value;
        }
    }

    public static void main(String[] args) throws Exception {

        IntCounter counter = new IntCounter();

        final int threadsCount = 1000;

        try (ShutdownOnValueInRange scope = new ShutdownOnValueInRange(100, 120)) {
            for (int i = 0; i < threadsCount; ++i) {
                final int id = i;
                scope.fork(() -> {
                    counter.inc();
//                    if (id == 400) {
//                        throw new IllegalStateException("My custom exception");
//                    }
                    return id;
                });
            }

            scope.join();
            scope.throwIfFailed();

            System.out.printf("Price found: %d\n", scope.getPrice());
        }


        System.out.printf("counter: %d\n", counter.getValue());

        System.out.printf("Main done... Java version used: %s\n", System.getProperty("java.version"));
    }

    /**
     * Custom implementation of 'StructuredTaskScope'.
     * Main logic handled inside 'handleComplete' method.
     */
    static class ShutdownOnValueInRange extends StructuredTaskScope<Integer> {

        final int from;
        final int to;

        public ShutdownOnValueInRange(int from, int to) {
            this.from = from;
            this.to = to;
        }

        private volatile int price;
        private volatile Exception ex;

        @Override
        protected void handleComplete(Future<Integer> future) {
            if (future.state() == Future.State.SUCCESS) {

                final int curPrice = future.resultNow();

                if (curPrice >= from && curPrice <= to) {
                    price = curPrice;
                    shutdown();
                }
            }
            else if (future.state() == Future.State.FAILED) {
                this.ex = (Exception) future.exceptionNow();
            }
        }

        public void throwIfFailed() throws Exception {
            if (ex != null) {
                throw ex;
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
