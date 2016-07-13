package thread.pools.fixed_size_thread_pools;

import java.util.LinkedList;

public class FixedSizeThreadPool {

    private final WorkerThread [] threads;

    private final LinkedList<Runnable> queue;

    public FixedSizeThreadPool(int numthreads) {
        threads = new WorkerThread[numthreads];
        queue = new LinkedList();
        startThreads();
    }

    private void startThreads() {
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new WorkerThread();
            threads[i].start();
        }
    }

    public void execute(Runnable task) throws InterruptedException {
        synchronized (queue) {
            queue.addLast(task);
            queue.notify();
        }
    }

    private Runnable getTask() {
        final Runnable r;
        while(true) {
            synchronized (queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                r = queue.removeFirst();
            }
            return r;
        }
    }

    private class WorkerThread extends Thread {

        @Override
        public void run() {
            while(true) {
                Runnable runnable = getTask();
                try {
                    runnable.run();
                } catch (RuntimeException error) {
                    System.out.println("Error : " + error.getMessage());
                }
            }
        }

    }


}
