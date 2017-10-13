package de.linearbits.suda2;

/**
 * Abstract base class providing rudimentary timing methods
 * @author RaffaelBild
 *
 */
public abstract class Timeable {
    
    /** TODO make timing of methods in various types generic via additional indices */
    
    /** Whether timing is enabled */
    private static final boolean ENABLED                 = true;

    /** Indices */
    public static final int      TYPE_INT_SET_BITS       = 0;

    public static final int      TYPE_INT_SET_HASH       = 1;

    public static final int      TYPE_INT_SET_SMALL      = 2;

    public static final int      TYPE_COUNT              = 3;

    public static final int      METHOD_PROJECTION       = 0;

    public static final int      METHOD_COUNT            = 1;

    /** Arrays */
    public static long[]         intersectionTime        = new long[TYPE_COUNT];

    public static long[]         intersectionCount       = new long[TYPE_COUNT];

    public static long[]         specialRowTime          = new long[TYPE_COUNT];

    public static long[]         specialRowCount         = new long[TYPE_COUNT];

    public static long[]         supportRowTime          = new long[TYPE_COUNT];

    public static long[]         supportRowCount         = new long[TYPE_COUNT];

    public static long[]         instanceCount           = new long[TYPE_COUNT];

    public static long[]         methodCallTime          = new long[METHOD_COUNT];

    public static long[]         methodCallCount         = new long[METHOD_COUNT];

    public static long[][]       intersectionSizeBuckets = new long[TYPE_COUNT][7];

    public static long[][]       intersectionTimeBuckets = new long[TYPE_COUNT][7];
    
    /** Only object variable */
    long time;
    
    /**
     * Returns the correct index of an intersection bucket for a given size.
     * Used for evaluations.
     * @param size
     */
    protected int getIntersectionBucketIndex(int size) {
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
    protected void endTiming(int method) {
        if (ENABLED) {
            methodCallTime[method] = System.nanoTime() - time;
            methodCallCount[method]++;
        }
    }
    
    /**
     * End timing of an intersection performed by a set with a given size
     * @param type
     * @param size
     */
    protected void endIntersectionTiming(int type, int size) {
        if (ENABLED) {
            long endTime = System.nanoTime() - time;
            intersectionTime[type] += endTime;
            intersectionCount[type]++;
            int bucket = getIntersectionBucketIndex(size);
            intersectionSizeBuckets[type][bucket]++;
            intersectionTimeBuckets[type][bucket] += endTime;
        }
    }
    
    /**
     * End timing of support rows
     * @param type
     * @param size
     */
    protected void endSupportRowTiming(int type) {
        if (ENABLED) {
            long endTime = System.nanoTime() - time;
            supportRowTime[type] += endTime;
            supportRowCount[type]++;
        }
    }
    
    /**
     * End timing of special rows
     * @param type
     * @param size
     */
    protected void endSpecialRowTiming(int type) {
        if (ENABLED) {
            long endTime = System.nanoTime() - time;
            specialRowTime[type] += endTime;
            specialRowCount[type]++;
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
        intersectionTime = new long[TYPE_COUNT];
        intersectionCount = new long[TYPE_COUNT];
        instanceCount = new long[TYPE_COUNT];
        methodCallTime = new long[METHOD_COUNT];
        methodCallCount = new long[METHOD_COUNT];
        intersectionSizeBuckets = new long[TYPE_COUNT][7];
        intersectionTimeBuckets = new long[TYPE_COUNT][7];
    }
}
