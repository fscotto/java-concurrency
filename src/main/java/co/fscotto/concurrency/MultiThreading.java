package co.fscotto.concurrency;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreading {
    private static final AtomicInteger ATOMIC_COUNTER = new AtomicInteger(0);
    private static final Object MONITOR_OBJ = new Object();
    private static int counter = 0;

    public static void main(String[] args) throws Exception {
        final Runnable atomicRunnable = () -> {
            for (int i = 0; i < 10_000; i++) {
                ATOMIC_COUNTER.incrementAndGet();
            }
        };
//        final Runnable runnable = () -> {
//            for (int i = 0; i < 10_000; i++) {
//                counter++;
//            }
//        };
        final Runnable runnable = () -> {
            for (int i = 0; i < 10_000; i++) {
                synchronized (MONITOR_OBJ) {
                    counter++;
                }
            }
        };

        final var t1 = new Thread(runnable);
        final var t2 = new Thread(runnable);

        t1.start();
        t1.join();

        t2.start();
        t2.join();

        System.out.printf("The primitive counter is %d%n", counter);

        try (final var pool = new ExecutorShutdownDecorator(Executors.newFixedThreadPool(2))) {
            pool.submit(atomicRunnable).get(1, TimeUnit.SECONDS);
            pool.submit(atomicRunnable).get(1, TimeUnit.SECONDS);
            System.out.printf("The atomic counter is %d%n", ATOMIC_COUNTER.intValue());
        }

        final var server = new PrintServer();
        server.print(new PrintJob("work1"));
        server.print(new PrintJob("work2"));
        server.print(new PrintJob("work3"));
    }

    record ExecutorShutdownDecorator(ExecutorService executor) implements AutoCloseable {

        public Future<?> submit(Runnable runnable) {
            return executor.submit(runnable);
        }

        @Override
        public void close() {
            executor.shutdown();
        }
    }

    public record PrintJob(String name) {
    }

    public static class PrintQueue {
        private final Queue<PrintJob> queue = new LinkedList<>();

        public synchronized void add(PrintJob job) {
            queue.add(job);
            notifyAll();
        }

        public synchronized PrintJob remove() throws InterruptedException {
            while (queue.isEmpty())
                wait();
            return queue.remove();
        }
    }

    public static class PrintServer {
        private final PrintQueue requests = new PrintQueue();

        public PrintServer() {
            Runnable service = () -> {
                while (true) {
                    try {
                        realPrint(requests.remove());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            };
            new Thread(service).start();
        }

        public void print(PrintJob job) {
            requests.add(job);
        }

        private void realPrint(PrintJob job) {
            // effettua la stampa
            System.out.printf("Stampo il job %s%n", job.name());
        }
    }
}
