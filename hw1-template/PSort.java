//UT-EID=mep3368 jp54694


import java.util.*;
import java.util.concurrent.*;

public class PSort {
    public static void parallelSort(int[] A, int begin, int end) {
        Sort sort = new Sort(A, begin, end);
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(processors);
        pool.invoke(sort);
    }

    public static class Sort extends RecursiveAction {
        int[] array;
        int begin;
        int end;

        public Sort(int[] array, int begin, int end) {
            this.array = array;
            this.begin = begin;
            this.end = end;
        }

        public Sort(int[] array) {
            this(array, 0, array.length);
        }

        @Override
        protected void compute() {
            if (begin - end <= 16) {
                insertionSort(array, begin, end);
            } else {
                int p = partition(array, begin, end);
                invokeAll(new Sort(array, begin, p), new Sort(array, p, end));
            }
        }

        public int partition(int[] A, int lo, int hi) {
            int pivotIndex = (hi + lo) / 2;
            int pivot = A[pivotIndex];
            int i = lo - 1;
            int j = hi + 1;
            while (true) {
                while (A[i] < pivot) {
                    i += 1;
                }
                while (A[j] > pivot) {
                    j -= 1;
                }
                if (i >= j)
                    return j;
                swap(A, i, j);
            }
        }

        public void insertionSort(int[] A, int begin, int end) {
            for (int i = begin + 1; i < end; i++) {
                int j = i;
                while (j > begin && (A[j - 1] > A[j])) {
                    swap(A, j, j - 1);
                    j--;
                }
            }
        }

        public void swap(int[] A, int i, int j) {
            int temp = A[i];
            A[i] = A[j];
            A[j] = temp;
        }
    }


}
