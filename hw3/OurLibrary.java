import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OurLibrary {
    int recordId;

    // BookName -> Count
    Map<String, Integer> inventory;

    // RecordId -> BookName
    // keeps a record of what BookName corresponds to RecordId
    Map<Integer, String> record = new ConcurrentHashMap<>();

    List<Student> borrowers;

    public OurLibrary(Map<String, Integer> inventory) {
        this.inventory = inventory;
        this.borrowers = Collections.synchronizedList(new ArrayList<>());
        recordId = 0;
    }

    public synchronized String returnBook(int recordId) {
        for (Student s : borrowers) {
            Iterator<Map.Entry<Integer, String>> iter = s.getBorrowed().entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Integer, String> entry = iter.next();
                if (entry.getKey() == recordId) {
                    iter.remove();
                    String bookName = record.get(recordId);
                    int newCount = inventory.get(bookName) + 1;
                    inventory.put(bookName, newCount);
                    return recordId + " is returned";
                }
            }
        }
        return recordId + " not found, no such borrow record";
    }

    public synchronized String inventory() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> me : inventory.entrySet()) {
            sb.append(me.getKey()).append(" ").append(me.getValue()).append('\n');
        }
        System.out.println("sb" + sb);
        return sb.toString();
    }

    public synchronized String list(String studentName) {
        for (Student s : borrowers) {
            if (s.toString().equals(studentName)) {
                return s.list();
            }
        }
        return "No record found for " + studentName;
    }

    /**
     * If valid:
     * Creates Student if not already present in borrowers, adds book to Student's "borrowed" list, updates
     * inventory, updates record
     * else:
     * return error code
     *
     * @param studentName name of the student that is doing the borrowing
     * @param bookName    name of the book the student wants to borrow
     * @return -1 if the library does not have the book, -2 if the library is out of the book, recordId if successful
     */
    public synchronized String borrow(String studentName, String bookName) {
        if (!inventory.containsKey(bookName)) {
            return "Request Failed - We do not have this book";
        } else if (inventory.get(bookName) == 0) {
            return "Request Failed - Book not available";
        } else {
            int copiesNow = inventory.get(bookName) - 1;
            boolean found = false;
            inventory.put(bookName, copiesNow);
            recordId++;
            for (Student s : borrowers) {
                if (s.toString().equals(studentName)) {
                    s.getBorrowed().put(recordId, bookName);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Student newStudent = new Student(studentName);
                newStudent.getBorrowed().put(recordId, bookName);
                borrowers.add(newStudent);
            }
            System.out.println("Borrowers: " + borrowers);
            record.put(recordId, bookName);
            return "Your request has been approved, " + recordId + " " + studentName + " " + bookName;
        }
    }

    public synchronized void exit() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("inventory.txt"));
            out.write(inventory());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
