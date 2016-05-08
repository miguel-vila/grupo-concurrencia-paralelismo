package atomic_ops;

import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        //final Supplier<Stack<Integer>> stackBuilder = () -> new SynchronizedStack();
        final Supplier<Stack<Integer>> stackBuilder = () -> new NonBlockingStack();
        final long runningTime = 1000;
        final int threads = 1;
        RandomStackOpsRunner runner = new RandomStackOpsRunner(threads, runningTime, stackBuilder);
        System.out.println("Starting!!!");
        long executedOps = runner.run();
        System.out.println(String.format("Executed ops during %d millis = %d", runningTime, executedOps));
    }

}
