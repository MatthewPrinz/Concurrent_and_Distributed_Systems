import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FactoryX extends Thread {
    private int w;
    private int d;
    private int metal, chem;
    private int items;

    public Lock mutex = new ReentrantLock();
    public Condition metalFull = mutex.newCondition();
    public Condition chemFull = mutex.newCondition();
    public Condition whFull = mutex.newCondition();
    public Condition whEmpty = mutex.newCondition();
    public Condition enoughMat = mutex.newCondition();

    public FactoryX(int warehouse_capacity, int depot_capacity) {
       this.w = warehouse_capacity;
       this.d = depot_capacity;
       this.metal = this.chem = 0;
       this.items = 0;
    }

    public void receiveMetal() throws InterruptedException {
        mutex.lock();
        while (d-metal == 3) {
            metalFull.wait();
        }

        metal++;
        if (metal >= 2 && chem >= 3) {
            enoughMat.signal();
        }
        mutex.unlock();
    }

    public void receiveChemical() throws InterruptedException {
        mutex.lock();
        while (d-chem == 2) {
            metalFull.wait();
        }

        chem++;
        if (metal >= 2 && chem >= 3) {
            enoughMat.signal();
        }
        mutex.unlock();
    }

    public void getProduct() throws InterruptedException {
        mutex.lock();
        while (items == 0) {
            whEmpty.wait();
        }

        items--;
        whFull.signal();
        mutex.unlock();
    }

    public void manufacture() throws InterruptedException {
        mutex.lock();
        while (items == w) {
            whFull.wait();
        }

        while (metal < 2 || chem < 3) {
            enoughMat.wait();
        }

        items++;
        metal -= 2;
        chem -= 3;
        whEmpty.signal();
        mutex.unlock();
    }
}