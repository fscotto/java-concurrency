package co.fscotto.concurrency;

public class HappensBefore {
    private static int x;
    private static volatile int g;

    public static void main(String[] args) throws InterruptedException {
        final Runnable run1 = () -> {
            x = 1;
            g = 1;
        };
        final Runnable run2 = () -> {
            var r1 = g;
            var r2 = x;
            System.out.printf("r1 = %d, r2 = %d%n", r1, r2);
        };

        var t1 = new Thread(run1);
        var t2 = new Thread(run2);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.printf("x = %d, g = %d%n", x, g);
    }
}
