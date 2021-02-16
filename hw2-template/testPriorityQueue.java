
public class testPriorityQueue implements Runnable {
    final static int SIZE = 30;
    final static int ROUND = 5;

    // final CyclicBarrier gate;
    final PriorityQueue pQueue;

    public testPriorityQueue(PriorityQueue pQueue) {
        this.pQueue = pQueue;
    }

    public void run() {
        for (int i = 0; i < ROUND; ++i) {

        }
    }

    public static void main(String[] args) {
        PriorityQueue pQueue = new PriorityQueue(SIZE);

        Thread[] t = new Thread[SIZE];

        for (int i = 0; i < SIZE; ++i) {
            t[i] = new Thread(new testCyclicBarrier(gate));
        }

        for (int i = 0; i < SIZE; ++i) {
            t[i].start();
        }
    }
}
