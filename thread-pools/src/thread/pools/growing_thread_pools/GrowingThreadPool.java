package thread.pools.growing_thread_pools;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by miguel on 7/06/16.
 */
public class GrowingThreadPool {

    private final AtomicReference<GrowingThreadPoolState> state;

    private final LinkedList<Worker> threads;

    private final LinkedList<Runnable> queue;

    public GrowingThreadPool(int initialThreads) throws InterruptedException {
        state = new AtomicReference(new GrowingThreadPoolState());
        queue = new LinkedList();
        threads = new LinkedList();
        for (int i = 0; i < initialThreads; i++) {
            System.out.println("pushing");
            pushNewWorker(null/*no initial task*/);
        }
    }

    public void execute(Runnable task) throws InterruptedException {
        GrowingThreadPoolState st = state.get();
        boolean allBusy = st.allAreBusy();
        System.out.println("allBusy = "+ allBusy);
        System.out.println("total = "+ st.total);
        if(allBusy) {
            pushNewWorker(task);
        } else {
            queue.add(task);
            queue.notify();
        }
    }

    private void pushNewWorker(Runnable initialTask) throws InterruptedException {
        final Worker worker = new Worker(initialTask);
        threads.add(worker);
        System.out.println("increasedTotalCount");
        increaseTotalCount();
        System.out.println("starting");
        worker.start();
        System.out.println("started");
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

    public int getTotalThreads() {
        return state.get().total;
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
                Runnable runnable = getTask();
                runTask(runnable);
            }

        }

    }

}
