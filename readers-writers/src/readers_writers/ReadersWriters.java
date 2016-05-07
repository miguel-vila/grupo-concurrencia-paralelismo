package readers_writers;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by miguel on 5/7/16.
 */
public class ReadersWriters {

    private final Lock      counterMutex    = new ReentrantLock();
    private final Condition readPhase       = counterMutex.newCondition();
    private final Condition writePhase      = counterMutex.newCondition();
    private int             resourceCounter = 0;

    private       int       value;
    private final int       readersAndWriters;
    private final int       times;

    public ReadersWriters(int value, int readersAndWriters, int times) {
        this.value             = value;
        this.readersAndWriters = readersAndWriters;
        this.times             = times;
    }

    public class Reader extends Thread {

        private final int times;

        public Reader(int times) {
            this.times = times;
        }

        @Override
        public void run() {
            for (int i = 0; i < times; i++) {
                try {
                    counterMutex.lock();
                        while(resourceCounter == -1) {
                            readPhase.wait();
                        }
                        resourceCounter++;
                    counterMutex.unlock();
                    System.out.println("READER: value = "+value+", other readers: "+resourceCounter);
                    counterMutex.lock();
                        resourceCounter--;
                        if(resourceCounter == 0) {
                            readPhase.signal();
                        }
                    counterMutex.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    class Writer extends Thread {

        private final int initialNewValue;
        private final int times;

        public Writer(int newValue, int times) {
            this.initialNewValue = newValue;
            this.times    = times;
        }

        @Override
        public void run() {
            for (int i = 0; i < times; i++) {
                try {
                    counterMutex.lock();
                        while(resourceCounter != 0) {
                            writePhase.wait();
                        }
                        resourceCounter = -1;
                    counterMutex.unlock();
                    final int newValue = initialNewValue + i ;
                    System.out.println("WRITER: newValue: "+ newValue + ", oldValue " + value + ", resourceCounter = "+resourceCounter);
                    value = newValue;
                    counterMutex.lock();
                        resourceCounter = 0;
                        readPhase.signalAll();
                        writePhase.signal();
                    counterMutex.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void run() throws InterruptedException {
        Reader [] readers = new Reader[readersAndWriters];
        Writer [] writers = new Writer[readersAndWriters];
        System.out.println("STARTING!!!");
        for (int i = 0; i < readersAndWriters; i++) {
            readers[i] = new Reader(times);
            readers[i].run();
            writers[i] = new Writer(i+1, times);
            writers[i].run();
        }
        System.out.println("JOINING!!!");
        for (int i = 0; i < readersAndWriters; i++) {
            readers[i].join();
            writers[i].join();
        }
        System.out.println("DONE!!!");
    }

    public static void main(String[] args) throws InterruptedException {
        ReadersWriters rws = new ReadersWriters(10, 10, 100);
        rws.run();
    }

}


