package org.example;

public class Supervisor implements Runnable {
    private final AbstractProgram abstractProgram;
    private Thread programThread;

    public Supervisor(AbstractProgram abstractProgram) {
        this.abstractProgram = abstractProgram;
    }

    @Override
    public void run() {
        System.out.println("Супервизор запущен");
        programThread = new Thread(abstractProgram);
        programThread.start();

        Object mutex = abstractProgram.getMutex();

        while (abstractProgram.isRunning()) {
            synchronized (mutex) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                Status status = abstractProgram.getStatus();
                switch (status) {
                    case STOPPING:
                        abstractProgram.pause();
                        restart();
                        break;
                    case FATAL_ERROR:
                        stop();
                        break;
                    default:
                        break;
                }
            }
        }
        System.out.println("Супервизор остановлен");
    }

    public void stop() {
        System.out.println("Супервизор: Критическая ошибка. программа остановлена.");
        abstractProgram.stop();
        programThread.interrupt();
    }

    public void restart() {
        System.out.println("Супервизор: Перезапуск программы");
        abstractProgram.resume();
        abstractProgram.setStatus(Status.RUNNING);
    }
}
