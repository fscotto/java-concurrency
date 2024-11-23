package co.fscotto.concurrency;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Philosopher extends Thread {
    private final String name;
    private final Chopstick first;
    private final Chopstick second;
    private final Random random;

    public Philosopher(String name, Chopstick left, Chopstick right) {
        super(name);
        this.name = name;
        this.first = left;
        this.second = right;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Thread.sleep(random.nextInt(1)); // THINK
                synchronized (first) {
                    synchronized (second) {
                        System.out.println(this.name + " is eating");
                        Thread.sleep(random.nextInt(1000)); // EAT
                    }
                }
                System.out.println(this.name + " have eaten");
            }
        } catch (InterruptedException _) {
            if (Thread.interrupted()) {
                System.out.println(name + " interrupted");
            }
        }
    }

    public record Chopstick(int id) {
    }

    public static void main(String[] args) {
        final var chopsticks = new Chopstick[5];
        for (var i = 1; i <= chopsticks.length; i++) {
            chopsticks[i - 1] = new Chopstick(i);
        }

        final var philosophers = new Philosopher[5];
        philosophers[0] = new Philosopher("Aristotele", chopsticks[0], chopsticks[1]);
        philosophers[1] = new Philosopher("Socrate", chopsticks[1], chopsticks[2]);
        philosophers[2] = new Philosopher("Voltaire", chopsticks[2], chopsticks[3]);
        philosophers[3] = new Philosopher("Pitagora", chopsticks[3], chopsticks[4]);
        philosophers[4] = new Philosopher("Platone", chopsticks[4], chopsticks[0]);

        final var deadlockChecker = new DeadlockChecker(philosophers);
        deadlockChecker.watch();

        for (var philosopher : philosophers) {
            philosopher.start();
        }
    }

    public static class DeadlockChecker {
        private final List<Thread> threads;

        public DeadlockChecker(Thread... threads) {
            this.threads = new ArrayList<>(List.of(threads));
        }

        public synchronized void addThread(Thread runnable) {
            threads.add(runnable);
        }

        public void watch() {
            if (threads == null || threads.isEmpty())
                throw new IllegalStateException("No threads are registered");

            Thread.ofPlatform().start(() -> {
                final var threadMXBean = ManagementFactory.getThreadMXBean();
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(Duration.ofSeconds(5));
                        final var deadlockedThreadsIds = threadMXBean.findDeadlockedThreads();
                        if (deadlockedThreadsIds != null) {
                            System.out.println("Deadlock found!!!");
                            for (var threadId : deadlockedThreadsIds) {
                                final var threadInfo = threadMXBean.getThreadInfo(threadId);
                                selectThread(threadInfo.getThreadName()).ifPresent(t -> {
                                    System.out.printf("Stopping thread %s%n", t.getName());
                                    t.interrupt(); // FIXME There isn't a way to force to stop another thread
                                });
                            }
                        }
                    } catch (InterruptedException _) {
                        Thread.interrupted();
                        System.out.println("Deadlock Checker is interrupted");
                    }
                }
            });
        }

        private Optional<Thread> selectThread(String threadName) {
            return threads
                    .stream()
                    .filter(t -> t.getName().equals(threadName))
                    .findAny();
        }
    }
}
