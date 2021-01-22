import java.util.*;
import java.util.stream.Collectors;

public class SortTest {
    public static void main(String[] args) {
//    int[] A1 = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
//    verifyParallelSort(A1);
//
//    int[] A2 = {1, 3, 5, 7, 9};
//    verifyParallelSort(A2);
//
//    int[] A3 = {13, 59, 24, 18, 33, 20, 11, 11, 13, 50, 10999, 97};
//    verifyParallelSort(A3);
//
//    int length = 10000;
//    int[] A4 = new int[length];
//    Random random = new Random(1);
//
//    for (int i = 0; i < length; i++)
//    {
//      A4[i] = random.nextInt();
//    }
//    verifyParallelSort(A4);
        int[] A5 = {1, 2, 3};
        int[] B1 = {4, 5, 6};
        verifyParallelMerge(A5, B1);
    }

    static void verifyParallelMerge(int[] A, int[] B) {
        List<Integer> listA = Arrays.stream(A).boxed().collect(Collectors.toList());
        List<Integer> listB = Arrays.stream(B).boxed().collect(Collectors.toList());
        listA.addAll(listB);
        listA.sort(Collections.reverseOrder());
        int[] array = listA.stream().mapToInt(i -> i).toArray();
        printArray(A);
        printArray(B);
        int[] C = new int[A.length + B.length];
        PMerge.parallelMerge(A, B, C, 5);
        boolean isSuccess = true;
        for (int i = 0; i < C.length; i++) {
            if (array[i] != C[i])  {
                System.out.println("Your parallel merge algorithm is not correct");
                System.out.println("Expect:");
                printArray(array);
                System.out.println("Your results:");
                printArray(C);
                isSuccess = false;
                break;
            }
        }
        if (isSuccess) {
            System.out.println("Great, your merge algorithm works for this test case");
        }
        System.out.println("=========================================================");
    }

    static void verifyParallelSort(int[] A) {
        int[] B = new int[A.length];
        System.arraycopy(A, 0, B, 0, A.length);

        System.out.println("Verify Parallel Sort for array: ");
        printArray(A);

        Arrays.sort(A);
        PSort.parallelSort(B, 0, B.length);

        boolean isSuccess = true;
        for (int i = 0; i < A.length; i++) {
            if (A[i] != B[i]) {
                System.out.println("Your parallel sorting algorithm is not correct");
                System.out.println("Expect:");
                printArray(A);
                System.out.println("Your results:");
                printArray(B);
                isSuccess = false;
                break;
            }
        }

        if (isSuccess) {
            System.out.println("Great, your sorting algorithm works for this test case");
        }
        System.out.println("=========================================================");
    }

    public static void printArray(int[] A) {
        for (int i = 0; i < A.length; i++) {
            if (i != A.length - 1) {
                System.out.print(A[i] + " ");
            } else {
                System.out.print(A[i]);
            }
        }
        System.out.println();
    }
}
