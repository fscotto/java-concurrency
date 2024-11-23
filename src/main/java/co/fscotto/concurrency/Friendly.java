package co.fscotto.concurrency;

public class Friendly {
    private final String name;
    private Friendly partner;

    public Friendly(String name) {
        this.name = name;
    }

    // Fix deadlock without synchronized on method signature
    //public synchronized void hug() {
    public void hug() {
        System.out.println(Thread.currentThread().getName() + " in " + name + ".hug() trying to invoke " + partner.name + ".hugBack()");
        synchronized (this) {
            partner.hugBack();
        }
    }

    public synchronized void hugBack() {
        System.out.println(Thread.currentThread().getName() + " in " + name + ".hugBack()");
    }

    public void becomeFriend(Friendly partner) {
        this.partner = partner;
    }

    public static void main(String[] args) {
        final Friendly gareth = new Friendly("Gareth");
        final Friendly cory = new Friendly("Cory");
        gareth.becomeFriend(cory);
        cory.becomeFriend(gareth);

        new Thread(gareth::hug, "T1").start();
        new Thread(cory::hug, "T2").start();
    }
}
