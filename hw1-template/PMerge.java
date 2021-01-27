//UT-EID=mep3368 jp54694


import java.util.*;
import java.util.concurrent.*;


public class PMerge {
  public static ExecutorService threadPool = Executors.newCachedThreadPool();

  public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
    List<Future> futures = new ArrayList<>();

    for (int i = 0; i < numThreads; i++) {
      futures.add(threadPool.submit(new PMergeThread(i, numThreads, A, B, C)));
    }

    for (int i = 0; i < numThreads; i++) {
      try {
        futures.get(i).get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  public static class PMergeThread implements Runnable {
    public int start;
    public int stride;
    public int[] A, B, C;

    PMergeThread(int start, int stride, int[] A, int [] B, int[] C) {
      this.start = start;
      this.stride = stride;
      this.A = A;
      this.B = B;
      this.C = C;
    }

    public void run() {
      for (int i = start; i < C.length; i+=stride) {
        if (i < A.length) {
          int rank = bisectLeft(B, A[i]);
          C[C.length-i-rank-1] = A[i];
        } else {
          int j = i-A.length;
          int rank = bisectRight(A, B[j]);
          C[C.length-j-rank-1] = B[j];
        }
      }
    }
  }

  public static int bisectLeft(int[] A, int T)
  {
    int L = 0;
    int R = A.length - 1;
    while (L <= R)
    {
      int m = (L+R)/2;
      if (A[m] < T)
        L = m+1;
      else if (A[m] >= T)
        R = m-1;
    }

    return L;
  }

  public static int bisectRight(int[] A, int T)
  {
    int L = 0;
    int R = A.length - 1;
    while (L <= R)
    {
      int m = (L+R)/2;
      if (A[m] <= T)
        L = m+1;
      else if (A[m] > T)
        R = m-1;
    }

    return L;
  }
}
