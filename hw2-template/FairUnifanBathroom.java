// EID 1 mep3368
// EID 2 jp54694

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

/*
(a)  The bathroom size is limited and can hold no more than 4 fans at any time.
(b)  The bathroom can only hold fans from one team at any time, otherwise a fight might break out.
More specifically, if there is a single UT fan in the bathroom there can be no OU fans in the bathroom and vice versa.
(c)  We require some sort of fairness in that a fan requesting the bathroom is blocked until all other fans preceding
them have entered the bathroom, regardless of the team (just as if fans were waiting in a line for the bathroom).
Use a ticketNumber that ensures this fairness.
Do not worry about the integer overflow for the ticket number.
 */
// TODO: try bathroom.locking around critical section? note - would have to reimplement try finally construct
// TODO: should leave be locked?
public class FairUnifanBathroom {
    // do we need to use ticket number or can we just pass the initializer "true"
    ReentrantLock bathroomLock = new ReentrantLock();
    final Condition fullBathroom = bathroomLock.newCondition();
    final Condition utInBathroom = bathroomLock.newCondition();
    final Condition ouInBathroom = bathroomLock.newCondition();
    final Condition notMyTurn = bathroomLock.newCondition();

    AtomicInteger fansInBathroom = new AtomicInteger();
    AtomicInteger utFansInBathroom = new AtomicInteger();
    AtomicInteger ouFansInBathroom = new AtomicInteger();
    AtomicInteger ticketNumber = new AtomicInteger();
    AtomicInteger lastTicketToLeave = new AtomicInteger();

    public void enterBathroomUT() {
        int yourTicketNumber = ticketNumber.incrementAndGet();
        bathroomLock.lock();
//        System.out.println("UT Thread: " + Thread.currentThread().getName() +  " Entering Bathroom, UT Fans inside: "
//                + utFansInBathroom.get() + " number of people currently inside: " + fansInBathroom.get() +
//                " myticketNumber = " + yourTicketNumber);
        try {
            while (yourTicketNumber > (lastTicketToLeave.get() + 1)) {
                try {
                    notMyTurn.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("1) UT Thread: " + Thread.currentThread().getName() + " UT Enter - Passed Ticket verification");
            while (ouFansInBathroom.get() > 0) {
                try {
                    utInBathroom.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("2) UT Thread: " + Thread.currentThread().getName() + " UT Enter - Passed OUFans check");
            while (fansInBathroom.get() >= 4) {
                try {
                    fullBathroom.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("3) UT Thread: " + Thread.currentThread().getName() + " UT Enter - Passed total fans check");
            utFansInBathroom.incrementAndGet();
            fansInBathroom.incrementAndGet();
        } finally {
            bathroomLock.unlock();
//            System.out.println("UT Thread: " + Thread.currentThread().getName() + " Enter - Unlocking the bathroom");
        }
    }

    public void enterBathroomOU() {
        int yourTicketNumber = ticketNumber.incrementAndGet();
        bathroomLock.lock();
//        System.out.println("OU Thread: " + Thread.currentThread().getName() +  " Entering Bathroom, OU Fans inside: "
//                + ouFansInBathroom.get() + " number of people currently inside: " + fansInBathroom.get() +
//                " myticketNumber = " + yourTicketNumber);
        try {
            while (yourTicketNumber > (lastTicketToLeave.get() + 1)) {
                try {
                    notMyTurn.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("1) OU Thread: " + Thread.currentThread().getName() + " Enter - Passed ticket verification ");
            while (utFansInBathroom.get() > 0) {
                try {
                    ouInBathroom.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("2) OU Thread: " + Thread.currentThread().getName() + " Enter - Passed UT Fans check ");
            while (fansInBathroom.get() >= 4) {
                try {
                    fullBathroom.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("3) OU Thread: " + Thread.currentThread().getName() + " Enter - Passed total Fans check ");
            ouFansInBathroom.incrementAndGet();
            fansInBathroom.incrementAndGet();
        } finally {
            bathroomLock.unlock();
//            System.out.println("OU Thread: " + Thread.currentThread().getName() + " Enter - Unlocking the bathroom");
        }
    }

    public void leaveBathroomUT() {
        bathroomLock.lock();
//        System.out.println(Thread.currentThread().getName() + " leaving UT Bathroom, number of people currently inside: "
//                + fansInBathroom.get());
        try {
            lastTicketToLeave.incrementAndGet();
            fansInBathroom.decrementAndGet();
            fullBathroom.signalAll();
            if (utFansInBathroom.decrementAndGet() == 0) {
                ouInBathroom.signalAll();
            }
            notMyTurn.signalAll();
        } finally {
            bathroomLock.unlock();
        }
    }

    public void leaveBathroomOU() {
        bathroomLock.lock();
//        System.out.println(Thread.currentThread().getName() + " leaving OU Bathroom, number of people currently inside: "
//                + fansInBathroom.get());
        try {
            lastTicketToLeave.incrementAndGet();
            fansInBathroom.decrementAndGet();
            fullBathroom.signalAll();
            if (ouFansInBathroom.decrementAndGet() == 0) {
                utInBathroom.signalAll();
            }
            notMyTurn.signalAll();
        } finally {
            bathroomLock.unlock();
        }
    }
}
	
