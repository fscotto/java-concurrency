package co.fscotto.concurrency;

public class Babble extends Thread {
    private static boolean doYield;
    private static int howOften;
    private final String word;

    public Babble(String whatToSay) {
        this.word = whatToSay;
    }

    @Override
    public void run() {
        for (int i = 0; i < howOften; i++) {
            System.out.println(word);
            if (doYield)
                Thread.yield();
        }
    }

    public static void main(String[] args) {
        doYield = Boolean.parseBoolean(args[0]);
        howOften = Integer.parseInt(args[1]);
        for (int i = 2; i < args.length; i++) {
            new Babble(args[i]).start();
        }
    }
}
