// EID 1
// EID 2

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
	final static int MIN_PRIORITY = 0;
	final static int MAX_PRIORITY = 9;

	// Underlying Linked List for Priority Queue
	Node llHead;
	AtomicInteger currSize = new AtomicInteger(0);
	int maxSize;

	// Locks and conditions for concurrency
	ReentrantLock capacityLock = new ReentrantLock();
	Condition notEmpty = capacityLock.newCondition();
	Condition notFull = capacityLock.newCondition();


	// implement fine-grained synchronization with lock on each node
	class Node {
		String name;
		int priority;
		Node next;
		ReentrantLock rel = new ReentrantLock();

		Node (String name, int priority, Node next) {
			this.name = name;
			this.priority = priority;
			this.next = next;
		}
	}

	public PriorityQueue(int maxSize) {
        // Creates a Priority queue with maximum allowed size as capacity
		// Initialize dummy variables at both start and end of Linked List
		this.llHead = new Node("", MAX_PRIORITY+1, null);
		this.llHead.next = new Node("", MIN_PRIORITY+1, null);
		this.maxSize = maxSize;
	}

	public int add(String name, int priority) {
        // Adds the name with its priority to this queue.
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.

		// check for non-full Linked List
		capacityLock.lock();
		while (currSize >= maxSize) {
			try {
				notFull.await();
			} catch (InterruptedException e) {
				System.out.println("Caught interrupted exception");
			}
		}
		currSize++;
		notEmpty.signal();
		capacityLock.unlock();

		// This method blocks when the list is full.
		Node prev = llHead;
		prev.rel.lock();
		Node curr = llHead.next;
		curr.rel.lock();

		// traverse to correct node (locking pairs along the way)
		int pos = 0;
		while (curr.priority <= priority) {
			if (name == curr.name) {
				prev.rel.unlock();
				curr.rel.unlock();

				// decrement size bc add unsuccessful
				capacityLock.lock();
				currSize--;
				notFull.notify();
				capacityLock.unlock();
				return -1;
			}

			pos++;
			prev.rel.unlock();
			prev = curr;
			curr = curr.next;
			curr.rel.lock();
		}


		// create and add node
		prev.next = new Node(name, priority, curr);
		prev.rel.unlock();
		curr.rel.unlock();

		return pos;
	}

	public int search(String name) {
        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
		Node temp = this.llHead;

		return 1;
	}

	public String getFirst() {
        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
		return "";
	}
}

