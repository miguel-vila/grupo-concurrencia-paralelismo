package thread.pools.growing_thread_pools;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by miguel on 7/06/16.
 */
public class SynchronousQueueGrowingThreadPool {

    private final AtomicReference<GrowingThreadPoolState> state;

    private final SynchronousQueue<Worker> threads;

    private final SynchronousQueue<Runnable> queue;

    public SynchronousQueueGrowingThreadPool(int initialThreads) throws InterruptedException {
        state = new AtomicReference(new GrowingThreadPoolState());
        queue = new SynchronousQueue();
        threads = new SynchronousQueue();
        for (int i = 0; i < initialThreads; i++) {
            pushNewWorker(null);
        }
    }

    public void execute(Runnable task) throws InterruptedException {
        GrowingThreadPoolState st = state.get();
        if(st.allAreBusy()) {
            pushNewWorker(task);
        }
        queue.put(task);
    }

    private void pushNewWorker(Runnable initialTask) throws InterruptedException {
        final Worker worker = new Worker(initialTask);
        increaseTotalCount();
        threads.put(worker);
        worker.start();
    }

    private void increaseTotalCount() {
        state.updateAndGet(GrowingThreadPoolState::increaseTotalCount);
    }

    private void increaseBusyCount() {
        state.updateAndGet(GrowingThreadPoolState::increaseBusyCount);
    }

    private void decreaseBusyCount() {
        state.updateAndGet(GrowingThreadPoolState::decreaseBusyCount);
    }

    private class Worker extends Thread {

        private final Runnable initialTask;

        public Worker(Runnable initialTask) {
            this.initialTask = initialTask;
        }

        public void runTask(Runnable task) {
            increaseBusyCount();
            try {
                task.run();
            } catch (RuntimeException error) {
                System.out.println("Error : " + error.getMessage());
            }
            decreaseBusyCount();
        }

        @Override
        public void run() {
            if(initialTask != null) {
                runTask(initialTask);
            }
            while(true) {
                Runnable runnable = queue.poll();
                runTask(runnable);
            }

        }

    }

}
