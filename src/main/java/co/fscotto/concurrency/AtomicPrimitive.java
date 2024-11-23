package co.fscotto.concurrency;

import java.security.SecureRandom;
import java.util.Random;

public class AtomicPrimitive {
    public static final Random RANDOM = new SecureRandom();
    private static int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        final Runnable writerTask = () -> counter = RANDOM.nextInt(1000) + 1;
        final Runnable readerTask = () -> System.out.println(Thread.currentThread().getName() + " read counter = " + counter);

        Thread.ofPlatform().start(writerTask);
        for (int i = 1; i <= 10; i++) {
            Thread.ofPlatform().name("T" + 1).start(readerTask);
        }

        final var spinner = new Spinner(5);
        Thread.ofPlatform().start(spinner);
        for (int i = 1; i <= 10; i++) {
            Thread.ofPlatform().name("T" + i).start(spinner::updateCurrent);
            Thread.sleep(10L * i * RANDOM.nextLong(1, 100));
        }

        System.out.println("Spinner update done.");
    }

    public static class Spinner implements Runnable {
        private int currentValue;

        public Spinner(int currentValue) {
            this.currentValue = currentValue;
        }

        public int getCurrentValue() {
            return currentValue;
        }

        public void updateCurrent() {
            currentValue = RANDOM.nextInt(1000) + 1;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println(getCurrentValue());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }
    }
}
