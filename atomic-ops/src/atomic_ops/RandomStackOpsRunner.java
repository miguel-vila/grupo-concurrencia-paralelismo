package atomic_ops;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RandomStackOpsRunner {

    private final int threads;

    private final Stack<Integer> stack;

    private final long runningMillis;

    private volatile boolean run = true;

    // shared among threads but only one thread per index
    private long[] executedOps;

    public RandomStackOpsRunner(int threads, long runningMillis, Supplier<Stack<Integer>> stackBuilder) {
        this.threads = threads;
        this.stack = stackBuilder.get();
        this.runningMillis = runningMillis;
        this.executedOps = new long[threads];
        Arrays.fill(executedOps, 0l);
    }

    private class StackOpThread extends Thread {

        private final int id;

        public StackOpThread(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            long operations = 0l;
            final long start = System.currentTimeMillis();
            while(System.currentTimeMillis() - start < runningMillis) {
                RandomStackOp.accept( stack );
                try {
                    Thread.sleep((long) (Math.random()*100));
                } catch (InterruptedException e) {

                }
                operations++;
            }
            executedOps[id] = operations;
            //System.out.println(String.format("ops = %d",operations));
        }

    }

    public long run() throws InterruptedException {
        Thread[] runningThreads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            runningThreads[i] =  new StackOpThread(i);
            runningThreads[i].start();
        }
        Timer timer = new Timer();
        TimerTask stopThreads = new TimerTask() {
            @Override
            public void run() {
                run = false;
            }
        };
        timer.schedule(stopThreads, runningMillis);
        for(Thread t: runningThreads) {
            t.join();
        }
        return Arrays.stream(executedOps).sum();
    }

    public static Consumer<Stack<Integer>> RandomStackOp = (stack) -> {
        // Que sea mas probable hacer un push que un pop
        if( Math.random() <= 0.3 ) {
            stack.pop();
        } else {
            stack.push(42);
        }
    };

}
