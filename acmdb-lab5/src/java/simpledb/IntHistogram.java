package simpledb;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {

    private int buckets, min, max, sum, width;
    private int[] bucketnum;

    /**
     * Create a new IntHistogram.
     *
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     *
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     *
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't
     * simply store every value that you see in a sorted list.
     *
     * @param buckets The number of buckets to split the input value into.
     * @param min The minimum integer value that will ever be passed to this class for histogramming
     * @param max The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
    	// some code goes here
        this.buckets = buckets;
        this.min = min;
        this.max = max;
        this.sum = 0;
        if (buckets <= max - min + 1)
            width = (int)Math.ceil(1.0 * (max - min + 1) / buckets);
        else
            width = 1;
        this.bucketnum = new int[buckets];
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
        bucketnum[(v - min) / width]++;
        sum++;
    	// some code goes here
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     *
     * For example, if "op" is "GREATER_THAN" and "v" is 5,
     * return your estimate of the fraction of elements that are greater than 5.
     *
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {
        int ans = 0;
        if (op == Predicate.Op.NOT_EQUALS) {
            if (v < min || v > max)
                return 1.0;
            return 1.0 - 1.0 * bucketnum[(v - min) / width] / width / sum;
        } else if (op == Predicate.Op.EQUALS) {
            if (v < min || v > max)
                return 0.0;
            return 1.0 * bucketnum[(v - min) / width] / width / sum;
        } else if (op == Predicate.Op.LESS_THAN) {
            if (v < min)
                return 0.0;
            if (v > max)
                return 1.0;
            int index = (v - min) / width;
            for (int i = 0; i < index; i++)
                ans += bucketnum[i];
            ans += bucketnum[index] * (v - min - (v - min) / width * width) / width;
        } else if (op == Predicate.Op.LESS_THAN_OR_EQ) {
            if (v < min)
                return 0.0;
            if (v > max)
                return 1.0;
            int index = (v - min) / width;
            for (int i = 0; i <= index; i++)
                ans += bucketnum[i];
            ans += bucketnum[index] * (v - min - (v - min) / width * width) / width;
        } else if (op == Predicate.Op.GREATER_THAN) {
            if (v < min)
                return 1.0;
            if (v > max)
                return 0.0;
            int index = (v - min) / width;
            for (int i = index + 1; i < buckets; i++)
                ans += bucketnum[i];
            ans += bucketnum[index] * (min + ((v - min) / width + 1) * width - 1 - v) / width;
        } else if (op == Predicate.Op.GREATER_THAN_OR_EQ) {
            if (v < min)
                return 1.0;
            if (v > max)
                return 0.0;
            int index = (v - min) / width;
            for (int i = index; i < buckets; i++)
                ans += bucketnum[i];
            ans += bucketnum[index] * (min + ((v - min) / width + 1) * width - 1 - v) / width;
        }
        return 1.0 * ans / sum;
    	// some code goes here
    }

    /**
     * @return
     *     the average selectivity of this histogram.
     *
     *     This is not an indispensable method to implement the basic
     *     join optimization. It may be needed if you want to
     *     implement a more efficient optimization
     * */
    public double avgSelectivity()
    {
        // some code goes here
        return 1.0;
    }

    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {
        // some code goes here
        return null;
    }
}
