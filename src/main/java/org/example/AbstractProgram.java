package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AbstractProgram implements Runnable {
//    private static final int timeout = 2000;
    private Status status;
    private final Object mutex = new Object();
    private final Object pauseMutex = new Object();
    private volatile boolean isRunning;
    private volatile boolean paused = false;
    private static final List<Status> statuses = new ArrayList<>(List.of(Status.RUNNING, Status.STOPPING, Status.FATAL_ERROR));

    public AbstractProgram() {
        isRunning = true;
        status = Status.UNKNOWN;
    }

    @Override
    public void run() {
        System.out.println("Запущена абстрактная программа");
        Thread daemon = new Thread(new DaemonTask(this));
        daemon.setDaemon(true);
        daemon.start();
        try {
            while (isRunning) {
                someWork();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Абстрактная программа завершена");
    }

    static class DaemonTask implements Runnable {


        private final Random random = new Random();
        private final AbstractProgram program;

        DaemonTask(AbstractProgram program) {
            this.program = program;
        }

        @Override
        public void run() {
            Status randomStatus;
            while (program.isRunning) {
                Utils.pause(2000);
                randomStatus = statuses.get(random.nextInt(statuses.size()));
                program.setStatus(randomStatus);
            }
        }
    }

    private void someWork() throws InterruptedException {
        synchronized (pauseMutex) {
            System.out.println("Абстрактная программа выполняет работу");
            while (paused) {
                pauseMutex.wait();
            }
        }
        Utils.pause(1000);
    }

    public Status getStatus() {
        synchronized (mutex) {
            return status;
        }
    }

    public void setStatus(Status status) {
        synchronized (mutex) {
            this.status = status;
            System.out.println("Демон: Статус абстрактной программы изменен на " + status);
            mutex.notifyAll();
        }
    }

    public void pause() {
        synchronized (pauseMutex) {
            paused = true;
        }
    }

    public void stop() {
        isRunning = false;
    }

    public void resume() {
        synchronized (pauseMutex) {
            paused = false;
            pauseMutex.notifyAll();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Object getMutex() {
        return mutex;
    }
}
