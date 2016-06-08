package thread.pools.fixed_size_thread_pools;

import java.util.concurrent.SynchronousQueue;

public class SynchronousQueueThreadPool {

    private final WorkerThread [] threads;

    private final SynchronousQueue<Runnable> queue;

    public SynchronousQueueThreadPool(int numthreads) {
        threads = new WorkerThread[numthreads];
        queue = new SynchronousQueue();
        startThreads();
    }

    private void startThreads() {
        for(WorkerThread thread: threads) {
            thread.start();
        }
    }

    public void execute(Runnable task) throws InterruptedException {
        queue.put(task);
    }

    private class WorkerThread extends Thread {

        @Override
        public void run() {
            while(true) {
                Runnable runnable = queue.poll();
                try {
                    runnable.run();
                } catch (RuntimeException error) {
                    System.out.println("Error : " + error.getMessage());
                }
            }
        }

    }


}
