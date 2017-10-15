package de.linearbits.suda2;

/**
 * Abstract base class providing rudimentary timing methods
 * @author RaffaelBild
 *
 */
public abstract class Timeable {
    
    /** Whether timing is enabled */
    private static final boolean ENABLED                    = false;

    /** Indices */
    public static final int      TYPE_INT_SET_BITS          = 0;

    public static final int      TYPE_INT_SET_HASH          = 1;

    public static final int      TYPE_INT_SET_SMALL         = 2;

    public static final int      TYPE_COUNT                 = 3;

    public static final int      METHOD_PROJECTION          = 0;

    public static final int      METHOD_COUNT               = 1;

    public static final int      TYPE_METHOD_SPECIALROW     = 0;

    public static final int      TYPE_METHOD_SUPPORTROW     = 1;
    
    public static final int      TYPE_METHOD_INTERSECTION   = 2;

    public static final int      TYPE_METHOD_COUNT          = 3;

    /** Arrays */

    public static long[]         instanceCount              = new long[TYPE_COUNT];

    public static long[]         methodCallTime             = new long[METHOD_COUNT];

    public static long[]         methodCallCount            = new long[METHOD_COUNT];

    public static long[][]       typeMethodCallTime         = new long[TYPE_COUNT][METHOD_COUNT];

    public static long[][]       typeMethodCallCount        = new long[TYPE_COUNT][METHOD_COUNT];

    public static long[][][]     typeMethodSizeCountBuckets = new long[TYPE_COUNT][METHOD_COUNT][7];

    public static long[][][]     typeMethodSizeTimeBuckets  = new long[TYPE_COUNT][METHOD_COUNT][7];
    
    /** Only object variable */
    long time;
    
    /**
     * Returns the correct index of a bucket for a given size
     * @param size
     */
    protected int getBucketIndex(int size) {
        if (size <= 10) return 0;
        else if (size <= 100) return 1;
        else if (size <= 1000) return 2;
        else if (size <= 10000) return 3;
        else if (size <= 100000) return 4;
        else if (size <= 1000000) return 5;
        else return 6;
    }

    /**
     * End timing of a method invocation
     * @param method
     */
    protected void endMethodTiming(int method) {
        if (ENABLED) {
            methodCallTime[method] = System.nanoTime() - time;
            methodCallCount[method]++;
        }
    }
    
    /**
     * End timing of a method of a type with a given size
     * @param type
     * @param size
     */
    protected void endTypeMethodTiming(int type, int method, int size) {
        if (ENABLED) {
            long endTime = System.nanoTime() - time;
            typeMethodCallTime[type][method] += endTime;
            typeMethodCallCount[type][method]++;
            int bucket = getBucketIndex(size);
            typeMethodSizeCountBuckets[type][method][bucket]++;
            typeMethodSizeTimeBuckets[type][method][bucket] += endTime;
        }
    }

    /**
     * Start timing
     */
    protected void startTiming() {
        if (ENABLED) {
            time = System.nanoTime();
        }
    }
    
    /**
     * Track the creation of an instance
     * @param type
     */
    protected void instance(int type) {
        if (ENABLED) {
            instanceCount[type]++;
        }
    }
    
    /**
     * Resets all timers
     */
    public static void reset() {
        instanceCount              = new long[TYPE_COUNT];
        methodCallTime             = new long[METHOD_COUNT];
        methodCallCount            = new long[METHOD_COUNT];
        typeMethodCallTime         = new long[TYPE_COUNT][METHOD_COUNT];
        typeMethodCallCount        = new long[TYPE_COUNT][METHOD_COUNT];
        typeMethodSizeCountBuckets = new long[TYPE_COUNT][METHOD_COUNT][7];
        typeMethodSizeTimeBuckets  = new long[TYPE_COUNT][METHOD_COUNT][7];
    }
}
