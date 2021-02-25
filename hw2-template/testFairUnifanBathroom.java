import java.util.Random;

public class testFairUnifanBathroom implements Runnable {
    final static int SIZE = 30;

    final FairUnifanBathroom fairUnifanBathroom;
    public testFairUnifanBathroom(FairUnifanBathroom fairUnifanBathroom) {
        this.fairUnifanBathroom = fairUnifanBathroom;
//        System.out.println("testFairUnifamBathroom constructor, fairunifambathroom hashcode: " + fairUnifanBathroom.hashCode());
    }

    public void run() {
        System.out.println("name: " + Thread.currentThread().getName());
        if (Integer.parseInt(Thread.currentThread().getName()) >= 60)
        {
//            System.out.println("UT Thread " + Thread.currentThread().getName() + " calling entering bathroom");
            fairUnifanBathroom.enterBathroomUT();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            System.out.println("UT Thread " + Thread.currentThread().getName() + " calling leaving bathroom");
            fairUnifanBathroom.leaveBathroomUT();
        }
        else
        {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("OU Thread " + Thread.currentThread().getName() + " calling entering bathroom");
            fairUnifanBathroom.enterBathroomOU();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            System.out.println("OU Thread " + Thread.currentThread().getName() + " calling leaving bathroom");
            fairUnifanBathroom.leaveBathroomOU();
        }
    }

    public static void main(String[] args) {
        FairUnifanBathroom fairUnifanBathroom = new FairUnifanBathroom();

        Thread[] t = new Thread[SIZE];
        final Random random = new Random(100);
        for (int i = 0; i < SIZE; ++i) {
            t[i] = new Thread(new testFairUnifanBathroom(fairUnifanBathroom));
            t[i].setName(Integer.toString(random.nextInt(100)));
        }

        for (int i = 0; i < SIZE; ++i) {
            t[i].start();
        }
    }
}
