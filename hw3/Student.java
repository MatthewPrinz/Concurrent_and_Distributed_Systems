import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Student {
    String name;
    // RecordId -> BookName
    Map<Integer, String> borrowed;

    public Student(String name)
    {
        this.name = name;
        this.borrowed = new ConcurrentHashMap<>();
    }

    public Map<Integer, String> getBorrowed() {
        return borrowed;
    }

    public String list()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String> me : borrowed.entrySet())
        {
            sb.append(me.getKey()).append(" ").append(me.getValue()).append('\n');
        }
        // Removing last '\n' due to how BookClient writes lines
        return sb.substring(0, sb.length()-1);
    }

    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return name.equals(student.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
