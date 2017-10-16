package de.linearbits.suda2;

/**
 * Abstract base class providing rudimentary timing methods
 * 
 * @author Raffael Bild
 * @author Fabian Prasser
 */
public abstract class Timeable {
    
    /** Whether timing is enabled */
    private static final boolean ENABLED                    = false;

    /** Index */
    public static final int      TYPE_INT_SET_BITS          = 0;
    /** Index */
    public static final int      TYPE_INT_SET_HASH          = 1;
    /** Index */
    public static final int      TYPE_INT_SET_SMALL         = 2;
    /** Index */
    public static final int      TYPE_COUNT                 = 3;
    /** Index */
    public static final int      METHOD_PROJECTION          = 0;
    /** Index */
    public static final int      METHOD_COUNT               = 1;
    /** Index */
    public static final int      TYPE_METHOD_SPECIALROW     = 0;
    /** Index */
    public static final int      TYPE_METHOD_SUPPORTROW     = 1;
    /** Index */
    public static final int      TYPE_METHOD_INTERSECTION   = 2;
    /** Index */
    public static final int      TYPE_METHOD_COUNT          = 3;

    /** Data */
    public static long[]         instanceCount              = new long[TYPE_COUNT];
    /** Data */
    public static long[]         methodCallTime             = new long[METHOD_COUNT];
    /** Data */
    public static long[]         methodCallCount            = new long[METHOD_COUNT];
    /** Data */
    public static long[][]       typeMethodCallTime         = new long[TYPE_COUNT][TYPE_METHOD_COUNT];
    /** Data */
    public static long[][]       typeMethodCallCount        = new long[TYPE_COUNT][TYPE_METHOD_COUNT];
    /** Data */
    public static long[][][]     typeMethodSizeCountBuckets = new long[TYPE_COUNT][TYPE_METHOD_COUNT][7];
    /** Data */
    public static long[][][]     typeMethodSizeTimeBuckets  = new long[TYPE_COUNT][TYPE_METHOD_COUNT][7];
    
    /**
     * Timing overview
     */
    public static void printOverview() {
        System.out.println(" - Hash");
        printTypeMethodOverview("Intersection", TYPE_INT_SET_HASH, TYPE_METHOD_INTERSECTION);
        printTypeMethodOverview("SpecialRow", TYPE_INT_SET_HASH, TYPE_METHOD_SPECIALROW);
        System.out.println(" - Bits");
        printTypeMethodOverview("Intersection", TYPE_INT_SET_BITS, TYPE_METHOD_INTERSECTION);
        printTypeMethodOverview("SpecialRow", TYPE_INT_SET_BITS, TYPE_METHOD_SPECIALROW);
        System.out.println(" - Jump");
        printTypeMethodOverview("Intersection", TYPE_INT_SET_SMALL, TYPE_METHOD_INTERSECTION);
        printTypeMethodOverview("SpecialRow", TYPE_INT_SET_SMALL, TYPE_METHOD_SPECIALROW);
    }
    
    /**
     * Timing overview
     */
    private static void printTypeMethodOverview(String label, int type, int method) {
        double ops = typeMethodCallCount[type][method] == 0d ? 0d : typeMethodCallTime[type][method] / typeMethodCallCount[type][method];
        double tmTime = (int)(typeMethodCallTime[type][method] / 1000000d);
        System.out.println("   * " + label+ ": " + tmTime +" ms, " + typeMethodCallCount[type][method] + " ops (" + ops +" ns / op)");
    }

    /**
     * Resets all timers
     */
    public static void reset() {
        instanceCount              = new long[TYPE_COUNT];
        methodCallTime             = new long[METHOD_COUNT];
        methodCallCount            = new long[METHOD_COUNT];
        typeMethodCallTime         = new long[TYPE_COUNT][TYPE_METHOD_COUNT];
        typeMethodCallCount        = new long[TYPE_COUNT][TYPE_METHOD_COUNT];
        typeMethodSizeCountBuckets = new long[TYPE_COUNT][TYPE_METHOD_COUNT][7];
        typeMethodSizeTimeBuckets  = new long[TYPE_COUNT][TYPE_METHOD_COUNT][7];
    }
    
    /** Time stamp*/
    long time;

    /**
     * Returns the index of a bucket for a given size
     * @param size
     */
    private int getBucketIndex(int size) {
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
     * End timing of a method of a type with a given size
     * @param type
     * @param size
     */
    protected void endTiming(int type, int method, int size) {
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
     * Track the creation of an instance
     * @param type
     */
    protected void instance(int type) {
        if (ENABLED) {
            instanceCount[type]++;
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
}
