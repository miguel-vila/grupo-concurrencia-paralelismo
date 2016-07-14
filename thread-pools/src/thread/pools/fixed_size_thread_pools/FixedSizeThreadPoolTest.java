package thread.pools.fixed_size_thread_pools;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FixedSizeThreadPoolTest {

    public Runnable createSleepTask(int millis, boolean [] executed, int taskId, CountDownLatch latch) {
        return () -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                executed[taskId] = true;
                latch.countDown();
            }
        };
    }

    @Test
    public void test1() throws InterruptedException {
        FixedSizeThreadPool pool = new FixedSizeThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] executed = {false};
        Runnable task = createSleepTask(150, executed, 0, latch);

        pool.execute(task);
        latch.await();
        assertTrue(executed[0]);
    }

    @Test
    public void test2() throws InterruptedException {
        FixedSizeThreadPool pool = new FixedSizeThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        final boolean[] executed = {false, false};
        Runnable task1 = createSleepTask(150, executed, 0, latch);
        Runnable task2 = createSleepTask(250, executed, 1, latch);

        pool.execute(task1);
        pool.execute(task2);
        latch.await(300, TimeUnit.MILLISECONDS);
        assertTrue(executed[0]);
        assertTrue(executed[1]);
    }
}
