// EID 1 mep3368
// EID 2 jp54694

import java.util.Collection;
import java.util.Map;
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
    final Condition fullBathroom = bathroomLock.newCondition();
    final Condition utInBathroom = bathroomLock.newCondition();
    final Condition ouInBathroom = bathroomLock.newCondition();
    final Condition notMyTurn = bathroomLock.newCondition();

    AtomicInteger fansInBathroom = new AtomicInteger();
    AtomicInteger utFansInBathroom = new AtomicInteger();
    AtomicInteger ouFansInBathroom = new AtomicInteger();
    AtomicInteger ticketNumber = new AtomicInteger();
    AtomicInteger lastTicketToLeave = new AtomicInteger();

    public synchronized void enterBathroomUT() {
        int yourTicketNumber = ticketNumber.incrementAndGet();
        bathroomLock.lock();
        try {
            while (yourTicketNumber > (lastTicketToLeave.get() + 4)) {
                try {
                    notMyTurn.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (ouFansInBathroom.get() > 0) {
                try {
                    utInBathroom.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (fansInBathroom.get() >= 4) {
                try {
                    fullBathroom.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            utFansInBathroom.incrementAndGet();
            fansInBathroom.incrementAndGet();
        } finally {
            bathroomLock.unlock();
        }
    }

    public synchronized void enterBathroomOU() {
        int yourTicketNumber = ticketNumber.incrementAndGet();
        bathroomLock.lock();
        try {
            while (yourTicketNumber > (lastTicketToLeave.get() + 4)) {
                try {
                    notMyTurn.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (utFansInBathroom.get() > 0) {
                try {
                    ouInBathroom.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (fansInBathroom.get() >= 4) {
                try {
                    fullBathroom.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ouFansInBathroom.incrementAndGet();
            fansInBathroom.incrementAndGet();
        } finally {
            bathroomLock.unlock();
        }
    }

    public synchronized void leaveBathroomUT() {
        bathroomLock.lock();
        try {
            lastTicketToLeave.incrementAndGet();
            fansInBathroom.decrementAndGet();
            fullBathroom.signalAll();
            if (utFansInBathroom.decrementAndGet() == 0) {
                ouInBathroom.signalAll();
            }
        }
        finally
        {
            bathroomLock.unlock();
        }
    }

    public synchronized void leaveBathroomOU() {
        bathroomLock.lock();
        try {
            lastTicketToLeave.incrementAndGet();
            fansInBathroom.decrementAndGet();
            fullBathroom.signalAll();
            if (ouFansInBathroom.decrementAndGet() == 0) {
                utInBathroom.signalAll();
            }
        }
        finally
        {
            bathroomLock.unlock();
        }
    }
}
	
