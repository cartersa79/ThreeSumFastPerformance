import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.util.Arrays;


public class ThreeSumFastPerformance {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    /* define constants */
    static long MAXVALUE = 2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 20;  // # of trials to run, more trials 'smooths out' quirks caused by random data
    static int MAXINPUTSIZE = (int) Math.pow(2, 14); // Times are MUCH faster than brute force algorithm
    static int MININPUTSIZE = 1;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/steve/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    public static void main(String[] args) {
        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("ThreeSumFast-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("ThreeSumFast-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("ThreeSumFast-Exp3.txt");

        // verify that the algorithm works
        System.out.println("");
        System.out.println("----Verification Test----");
        boolean is_valid = verifyThreeSumFast();
        System.out.print(is_valid);
    }

    static void runFullExperiment(String resultsFileName) {
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for (int inputSize = MININPUTSIZE; inputSize <= MAXINPUTSIZE; inputSize *= 2) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of random integers in random order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            //System.out.print("    Generating test data...");
            //long[] testList = createRandomIntegerList(inputSize);
            //System.out.println("...done.");
            //System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();

            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopWatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random list of integers each trial
                long[] testList = createRandomIntegerList(inputSize);

                // generate a random key to search in the range of a the min/max numbers in the list
                // long testSearchKey = (long) (0 + Math.random() * (testList[testList.length - 1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                long randomArray = threeSumFast(testList);
                batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            //batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n", inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    // implement threeSumFast algorithm from text
    public static long threeSumFast(long[] a) {
        // count triples that sum to 0.
        Arrays.sort(a);  // sort array
        long n = a.length;  // get number of items in the array
        long count = 0;  // initialize the counter that keeps track of valid threeSum combinations
        for (int i = 0; i < n; i++)  // two nested loops to try various combinations of array values
            for (int j = i + 1; j < n; j++)
                if (binarySearch(-(a[i]+a[j]), a) > j) // if negative of sum of i and j is found later in the array
                    count ++;                          // increment count
        return count;  // return number of valid combinations to caller
    }

    public static long[] createRandomIntegerList(int size) {
        long[] newList = new long[size];
        // randomly picks a value between -25000 and 25000
        // this range was chosen to help ensure that valid triplets will exist
        for (int j = 0; j < size; j++) {
            newList[j] = (long) ((50000 * Math.random()) - 25000);
        }
        return newList;  // return the list to caller
    }

    // verify the threeSumFast function by using a known list with valid combinations
    private static boolean verifyThreeSumFast() {
        long[] array1 = new long[]{ 1,2,-3,10,11,-21,50,51,-101 };  // 3 valid combinations
        long[] array2 = new long[]{ 1,2,3,10,11,21,50,51,101 };  // no valid combinations
        System.out.println("Array1 = " + Arrays.toString(array1));
        System.out.println("Array1 = " + Arrays.toString(array2));
        long combinations1 = threeSumFast(array1);  // number of valid combinations
        long combinations2 = threeSumFast(array2);  // number of valid combinations
        if ((combinations1 > 0) && (combinations2 == 0)){
            System.out.println("The algorithm successfully evaluated the test arrays.");
            System.out.println("Array1 = " + combinations1 + ", Array2 = " + combinations2);

            return true;
        }
        else{
            System.out.println("The algorithm failed to correctly process the test arrays.");
            return false;
        }
    }

    // implement binarySearch algorithm from earlier work and lecture
    public static int binarySearch(long key, long[] list) {
        int i = 0;  // set i to beginning of list
        int j = list.length - 1;  // set j to end of list
        if (list[i] == key) return i;  // found it, return index
        if (list[j] == key) return j;  // found it, return index
        int k = (i + j) / 2;  // didn't find it, get midpoint of list
        while (j - i > 1) {  // keep splitting list while you still have one
            if (list[k] == key) return k;  // found it, return index
            else if (list[k] < key) i = k;  // didn't find it, new list is lower half of remaining list
            else j = k;  // didn't find it, new list is upper half of remaining list
            k = (i + j) / 2;  // get new midpoint
        }
        return -1;  // key was not in list
    }
}
