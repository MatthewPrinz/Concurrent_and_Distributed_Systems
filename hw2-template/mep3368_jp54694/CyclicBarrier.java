/*
 * EID's of group members
 * 
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class CyclicBarrier {
	int parties;
	int arrivalIdx;
	int numLeave;
	Semaphore mutex;
	Semaphore indexMutex = new Semaphore(1);
	Semaphore incMutex = new Semaphore(1);

	public CyclicBarrier(int parties) {
		this.parties = parties;
		this.arrivalIdx = parties-1;
		this.numLeave = 0;
		mutex = new Semaphore(parties, true);
	}
	
	public int await() throws InterruptedException {
		// get the arrival index
		mutex.acquire();
		indexMutex.acquire();
		int index = arrivalIdx;
		arrivalIdx--;
		indexMutex.release();

		// wait until all other threads receive arrival index
		while (arrivalIdx >= 0) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw e;
			}
		}

		incMutex.acquire();
		numLeave++;
		// the last thread to leave releases the CS to prevent
		// another thread to get wrong arrival index
		if (numLeave == parties) {
			arrivalIdx = parties-1;
			numLeave = 0;
			for (int i = 0; i < parties; i++) {
				mutex.release();
			}
		}

		incMutex.release();
	    return arrivalIdx;
	}
}
