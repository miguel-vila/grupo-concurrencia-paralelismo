package thread.pools.growing_thread_pools;

/**
 * Created by miguel on 7/06/16.
 */
public class GrowingThreadPoolState {

    private final int total;

    private final int busy;

    private GrowingThreadPoolState(int total, int busy) {
        this.total = total;
        this.busy = busy;
    }

    public GrowingThreadPoolState() {
        this.total = 0;
        this.busy = 0;
    }

    public GrowingThreadPoolState increaseBusyCount() {
        return new GrowingThreadPoolState(total, busy+1);
    }

    public GrowingThreadPoolState increaseTotalCount() {
        return new GrowingThreadPoolState(total+1, busy);
    }

    public GrowingThreadPoolState decreaseBusyCount() {
        return new GrowingThreadPoolState(total, busy-1);
    }

    public boolean allAreBusy() {
        return total == busy;
    }

}
