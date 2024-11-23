package co.fscotto.concurrency;

import java.util.concurrent.TimeUnit;

public class ShowJoin {

    public static void main(String[] args) {
        try {
            CalcThread calcThread = new CalcThread();
            calcThread.start();
            doSomethingElse();
            calcThread.join();
            System.out.println("result is " + calcThread.getResult());
        } catch (InterruptedException e) {
            System.out.println("No answer: interrupted");
        }
    }

    public static void doSomethingElse() throws InterruptedException {
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
    }
}

class CalcThread extends Thread {
    private double result;

    @Override
    public void run() {
        result = calculate();
    }

    public double getResult() {
        return result;
    }

    private double calculate() {
        return Math.random();
    }
}
