// EID 1
// EID 2

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
    final static int MIN_PRIORITY = 0;
    final static int MAX_PRIORITY = 9;

    // Underlying Linked List for Priority Queue
    Node head;
    AtomicInteger currSize = new AtomicInteger(0);
    int maxSize;

    // Locks and conditions for concurrency
    ReentrantLock capacityLock = new ReentrantLock();
    final Condition empty = capacityLock.newCondition();
    final Condition full = capacityLock.newCondition();
    Set<String> names = new HashSet<>();

    // implement fine-grained synchronization with lock on each node
    class Node {
        String name;
        int priority;
        Node next;
        ReentrantLock rel = new ReentrantLock();

        Node(String name, int priority, Node next) {
            this.name = name;
            this.priority = priority;
            this.next = next;
        }
    }

    public PriorityQueue(int maxSize) {
        // Creates a Priority queue with maximum allowed size as capacity
        // Initialize dummy variables at both start and end of Linked List
        this.head = new Node("", MAX_PRIORITY + 1, null);
        this.maxSize = maxSize;
    }

    public int add(String name, int priority) {
        // Adds the name with its priority to this queue.
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.

        // check for non-full Linked List
        capacityLock.lock();
        while (currSize.get() == maxSize) {
            try {
                full.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        empty.signalAll();
        if (names.contains(name))
            return -1;
        capacityLock.unlock();
        int pos = -1; // -1 because of our head
        Node temp = head;

        while (temp.priority > priority) {
            pos++;
            if (temp.next != null && temp.next.priority >= priority) {
                temp = temp.next;
            } else
                break;
        }
        try {
            temp.rel.lock();
            Node newNode;
            if (pos == currSize.get()) {
                newNode = new Node(name, priority, null);
            } else {
                newNode = new Node(name, priority, temp.next);
            }
            temp.next = newNode;
            this.names.add(name);
            currSize.incrementAndGet();
        } finally {
            temp.rel.unlock();
        }
        return pos;
    }


    public int search(String name) {
        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
        Node temp = this.head;
        int i = -1; // -1 because we have a MaxPrio + 1 head
        while (temp != null) {
            if (name.equals(temp.name) && i != -1) {
                return i;
            }
            i++;
            temp = temp.next;
        }
        return -1;
    }

    public String getFirst() {
        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
        capacityLock.lock();
        try {
            while (currSize.get() == 0) {
                try {
                    empty.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            head.rel.lock();
            String removed;
            try {
                removed = head.next.name;
                names.remove(head.next.name);
                if (currSize.get() >= 2)
                    head.next = this.head.next.next;
                else
                    head.next = null;
                currSize.decrementAndGet();
            } finally {
                head.rel.unlock();
            }
            full.signalAll();
            return removed;
        } finally {
            capacityLock.unlock();
        }
    }
}

