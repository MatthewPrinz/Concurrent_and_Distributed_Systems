/*
 * EID's of group members
 * 
 */

public class MonitorCyclicBarrier {
	int parties;
	int arrivalIdx;
	int numLeave = 0;

	public MonitorCyclicBarrier(int parties) {
		this.parties = parties;
		this.arrivalIdx = parties-1;
	}
	
	public synchronized int await() throws InterruptedException {
		while (numLeave > 0) {
			wait();
		}

		System.out.println("Thread " + Thread.currentThread().getId() + " got index:" + arrivalIdx);
		int index = arrivalIdx;
		arrivalIdx--;

		while (arrivalIdx >= 0) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}

		numLeave++;
		if (numLeave == parties) {
			arrivalIdx = parties-1;
			numLeave = 0;
		}
		notifyAll();
	    return index;
	}
}
