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
public class FairUnifanBathroom {
    // do we need to use ticket number or can we just pass the initializer "true"
    ReentrantLock bathroomLock = new ReentrantLock();
    final Condition notMyTurn = bathroomLock.newCondition();
    final Condition waiters = bathroomLock.newCondition();
    AtomicInteger fansInBathroom = new AtomicInteger();
    AtomicInteger utFansInBathroom = new AtomicInteger();
    AtomicInteger ouFansInBathroom = new AtomicInteger();
    AtomicInteger ticketNumber = new AtomicInteger();
    AtomicInteger lastTicketToLeave = new AtomicInteger();

    public void enterBathroomUT() {
        int yourTicketNumber = ticketNumber.incrementAndGet();
        bathroomLock.lock();
        try {
            while (yourTicketNumber > (lastTicketToLeave.get() + 1)) {
                try {
                    notMyTurn.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (ouFansInBathroom.get() > 0 || fansInBathroom.get() >= 4) {
                try {
                    waiters.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            utFansInBathroom.incrementAndGet();
            fansInBathroom.incrementAndGet();
            lastTicketToLeave.incrementAndGet();
//            System.out.println("UT Thread (" + Thread.currentThread().getName() + ") finished incrementing variables. UTFansInBathroom: "
//                    + utFansInBathroom.get() + " OUFansInBathroom: " + ouFansInBathroom.get() +
//                    " totalFansInBathroom: " + fansInBathroom.get() + " ticketNumber: " + yourTicketNumber +
//                    " LastTicketToLeave: " + lastTicketToLeave.get());
        } finally {
            bathroomLock.unlock();
        }
    }

    public void enterBathroomOU() {
        int yourTicketNumber = ticketNumber.incrementAndGet();
        bathroomLock.lock();
        try {
            while (yourTicketNumber > (lastTicketToLeave.get() + 1)) {
                try {
                    notMyTurn.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (utFansInBathroom.get() > 0 || (fansInBathroom.get() >= 4)) {
                try {
                    waiters.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ouFansInBathroom.incrementAndGet();
            fansInBathroom.incrementAndGet();
            lastTicketToLeave.incrementAndGet();
//            System.out.println("OU Thread (" + Thread.currentThread().getName() + ") finished incrementing variables. UTFansInBathroom: "
//                    + utFansInBathroom.get() + " OUFansInBathroom: " + ouFansInBathroom.get() +
//                    " totalFansInBathroom: " + fansInBathroom.get() + " ticketNumber: " + yourTicketNumber +
//                    " LastTicketToLeave: " + lastTicketToLeave.get());
        } finally {
            bathroomLock.unlock();
        }
    }

    public void leaveBathroomUT() {
        bathroomLock.lock();
        try {
            fansInBathroom.decrementAndGet();
            if (utFansInBathroom.decrementAndGet() == 0) {
                waiters.signalAll();
            }
            notMyTurn.signalAll();
        } finally {
            bathroomLock.unlock();
        }
    }

    public void leaveBathroomOU() {
        bathroomLock.lock();
        try {
            fansInBathroom.decrementAndGet();
            if (ouFansInBathroom.decrementAndGet() == 0) {
                waiters.signalAll();
            }
            notMyTurn.signalAll();
        } finally {
            bathroomLock.unlock();
        }
    }
}
	
