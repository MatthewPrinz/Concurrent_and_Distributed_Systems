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
        this.head = new Node("start", MAX_PRIORITY + 1, null);
        this.head.next = new Node("end", MIN_PRIORITY-1, null);
        this.maxSize = maxSize;
    }

    public int add(String name, int priority) {
        // Adds the name with its priority to this queue.
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.
        // check for non-full Linked List
        capacityLock.lock();
        try {
            while (currSize.get() >= maxSize) {
                full.await();
            }
            currSize.incrementAndGet();
        } catch (InterruptedException e) {
            System.out.println("Caught interrupted exception");
        } finally {
            capacityLock.unlock();
        }

        // This method blocks when the list is full.
        Node prev = this.head;
        prev.rel.lock();
        Node curr = this.head.next;
        curr.rel.lock();

        // traverse to correct node (locking pairs along the way)
        int pos = 0;
        Node addNode = null;    // location to add if successful
        while (true) {
            if (name.equals(curr.name)) {
                prev.rel.unlock();
                curr.rel.unlock();
                if (addNode != null) {
                    addNode.rel.unlock();
                }

                // decrement size bc add unsuccessful
                capacityLock.lock();
                currSize.decrementAndGet();
                full.notify();
                capacityLock.unlock();
                return -1;
            }

            if (prev.priority >= priority && priority > curr.priority) {
                addNode = prev;
                addNode.rel.lock();
            }

            // hand over hand traversal
            pos++;
            prev.rel.unlock();
            prev = curr;
            curr = curr.next;
            if (curr == null) break;
            curr.rel.lock();
        }

        // create and add node
        capacityLock.lock();
        addNode.next = new Node(name, priority, addNode.next);
        System.out.println("successfully added node: " + name);
        prev.rel.unlock();
        addNode.rel.unlock();
        empty.signal();
        capacityLock.unlock();

        return pos;


    }


    public int search(String name) {
        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
        System.out.println("Searching for " + name);
        Node temp = this.head;
        int i = -1; // -1 because we have a MaxPriority + 1 head
        while (temp != null) {
            temp.rel.lock();
            System.out.println("Name: " + name + ", temp.name: " + temp.name);
            if (name.equals(temp.name) && i != -1) {
                temp.rel.unlock();
                return i;
            }
            i++;
            temp.rel.unlock();
            temp = temp.next;
        }
        return -1;
    }

    public String getFirst() {
        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
        try {
            capacityLock.lock();
            while (currSize.get() == 0) {
                empty.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            capacityLock.unlock();
        }

        String removed;
        try {
            this.head.rel.lock();
            this.head.next.rel.lock();
            this.head.next.rel.unlock();
            capacityLock.lock();
            removed = this.head.next.name;
            System.out.println("removing: " + removed);
            this.head.next = this.head.next.next;

            System.out.println("size before: " + currSize.get());
            currSize.decrementAndGet();
            System.out.println("size after: " + currSize.get());
            full.signal();
        } finally {
            this.head.rel.unlock();
            capacityLock.unlock();
        }

        return removed;
    }

    public void printPQueue() {
       Node temp = this.head.next;
       System.out.println("current priority queue:");
       while (temp.priority > MIN_PRIORITY) {
            System.out.println(temp.name);
       }
    }
}

