//UT-EID=mep3368 jp54694


import java.util.*;
import java.util.concurrent.*;


public class PMerge{
  public static void parallelMerge(int[] A, int[] B, int[]C, int numThreads){

  }

  public static int binarySearch(int[] A, int T)
  {
    int L = 0;
    int R = A.length - 1;
    while (L < R)
    {
      int m = (L+R)/2;
      if (A[m] < T)
        L = m+1;
      else if (A[m] > T)
        R = m+1;
      else
        return m;
    }
    return 0;
  }


}
