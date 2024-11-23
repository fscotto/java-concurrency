package co.fscotto.concurrency;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class Downloader extends Thread {
    private final URL url;
    private final String fileName;
    private final List<ProgressListener> listeners;

    public Downloader(URL url, String outputFilename) {
        this.url = url;
        this.fileName = outputFilename;
        this.listeners = new ArrayList<>();
    }

    public synchronized void addListener(ProgressListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(ProgressListener listener) {
        listeners.remove(listener);
    }

    private synchronized void updateProgress(int n) {
        for (ProgressListener listener : listeners) {
            listener.onProgress(n);
            System.out.println("Next progress listener");
        }
    }

    @Override
    public void run() {
        var n = 0;
        var total = 0;
        final var buffer = new byte[1024];
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(fileName)) {
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                total += n;
                System.out.printf("%s lock Downloader.updateProgress%n", Thread.currentThread().getName());
                updateProgress(total);
                System.out.printf("%s unlock Downloader.updateProgress%n", Thread.currentThread().getName());
            }
            out.flush();
        } catch (IOException _) {
            Thread.interrupted();
        }
    }

    public static void main(String[] args) throws IOException {
        final var url = URI.create("https://www.google.it").toURL();
        final var downloader1 = new Downloader(url, "file1.txt");
        final var downloader2 = new Downloader(url, "file2.txt");
        final var listener = new MyProgressListener(downloader1);
        downloader1.addListener(listener);
        downloader2.addListener(listener);
        downloader1.start();
        downloader2.start();
    }
}

interface ProgressListener {
    void onProgress(int n);
}

class MyProgressListener implements ProgressListener {
    private final Downloader target;

    public MyProgressListener(Downloader target) {
        this.target = target;
    }

    @Override
    public synchronized void onProgress(int n) {
        System.out.printf("Lock %s in ProgressListener.onProgress%n", Thread.currentThread().getName());
        synchronized (target) {
            System.out.printf("%s lock downloader field in the progress listener%n", Thread.currentThread().getName());
        }
        System.out.printf("Unlock %s in ProgressListener.onProgress%n", Thread.currentThread().getName());
    }
}
