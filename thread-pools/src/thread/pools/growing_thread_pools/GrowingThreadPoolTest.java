package thread.pools.growing_thread_pools;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by miguel on 7/06/16.
 */
public class GrowingThreadPoolTest {

    public Runnable sleepTask(int time) {
        return () -> {
            try {
                System.out.println("WAITING: "+time);
                Thread.sleep(time);
                System.out.println("DONE time: "+time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    @Test
    public void test1() throws InterruptedException {
        GrowingThreadPool pool = new GrowingThreadPool(1);
        Runnable task1 = sleepTask(1500);
        Runnable task2 = sleepTask(200);
        pool.execute(task1);

        pool.execute(task2);
        Thread.sleep(2000);
        assertEquals(2, pool.getTotalThreads());
    }

}