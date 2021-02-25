import java.util.Random;

public class testPriorityQueue implements Runnable {
    final static int NUMTHREADS = 4;
    final static int PQUEUESIZE = 2;
    // final CyclicBarrier gate;
    final PriorityQueue pQueue;

    public testPriorityQueue(PriorityQueue pQueue) {
        this.pQueue = pQueue;
    }

    public void run() {
        // bound is exclusive
        int priority = Integer.parseInt(Thread.currentThread().getName()) % 9;
        System.out.println("Thread " + Thread.currentThread().getName() + " is adding Node(" + Thread.currentThread().getName() + ", " + priority + ")");
        pQueue.add(Thread.currentThread().getName(), priority);
    }

    public static void main(String[] args) {
        PriorityQueue pQueue = new PriorityQueue(PQUEUESIZE);
        testReal(pQueue);
    }
    public static void testAdds(PriorityQueue pQueue)
    {
        pQueue.add("C", 7);
        pQueue.add("B", 8);
        pQueue.add("D", 5);
        pQueue.add("A", 9);
        for (int i = 0 ; i < 4; i++)
        {
            System.out.println(pQueue.getFirst());
        }
    }
    // make it more realistic
    public static void testReal(PriorityQueue pQueue)
    {
        final Random random = new Random(5);
        Thread[] t = new Thread[NUMTHREADS];
        for (int i = 0; i < NUMTHREADS; ++i) {
            t[i] = new Thread(new testPriorityQueue(pQueue));
            t[i].setName(Integer.toString(random.nextInt(10000)));
        }
        for (int i = 0; i < NUMTHREADS; ++i) {
            t[i].start();
        }
        // Give it some time to run...
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < NUMTHREADS; i++)
        {
            System.out.println(pQueue.getFirst());
        }
    }

    public static void testOrder(PriorityQueue pQueue)
    {
        pQueue.add("A", 9);
        pQueue.add("B", 9);
        for (int i = 0; i < 2; i++){
            System.out.println(pQueue.getFirst());
        }
    }
}
