
public class testFairUnifanBathroom implements Runnable {
    final static int SIZE = 10;

    final FairUnifanBathroom fairUnifanBathroom;

    public testFairUnifanBathroom(FairUnifanBathroom fairUnifanBathroom) {
        this.fairUnifanBathroom = fairUnifanBathroom;
//        System.out.println("testFairUnifamBathroom constructor, fairunifambathroom hashcode: " + fairUnifanBathroom.hashCode());
    }

    public void run() {
        if (Integer.parseInt(Thread.currentThread().getName()) % 2 == 0)
        {
            System.out.println("UT Thread " + Thread.currentThread().getName() + " calling entering bathroom");
            fairUnifanBathroom.enterBathroomUT();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("UT Thread " + Thread.currentThread().getName() + " calling leaving bathroom");
            fairUnifanBathroom.leaveBathroomUT();
        }
        else
        {
            System.out.println("OU Thread " + Thread.currentThread().getName() + " calling entering bathroom");
            fairUnifanBathroom.enterBathroomOU();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("OU Thread " + Thread.currentThread().getName() + " calling leaving bathroom");
            fairUnifanBathroom.leaveBathroomOU();
        }
    }

    public static void main(String[] args) {
        FairUnifanBathroom fairUnifanBathroom = new FairUnifanBathroom();

        Thread[] t = new Thread[SIZE];

        for (int i = 0; i < SIZE; ++i) {
            t[i] = new Thread(new testFairUnifanBathroom(fairUnifanBathroom));
            t[i].setName(Integer.toString(i));
        }

        for (int i = 0; i < SIZE; ++i) {
            t[i].start();
        }
    }
}
