package de.linearbits.suda2;

import java.util.Random;

import com.carrotsearch.hppc.IntOpenHashSet;

public class SUDA2IntSet {
    
    /** Default */
    private static final float DEFAULT_LOAD_FACTOR      = 0.75f;

    /** Default */
    private static final int   DEFAULT_INITIAL_CAPACITY = 8;

    public static void main(String[] args) {
        
        Random random = new Random(0xDEADBEEF);
        long time = System.currentTimeMillis();
        for (int r=0; r<100; r++) {
            SUDA2IntSet set = new SUDA2IntSet();
            for (int k=0; k<1000000; k++) {
                set.add(random.nextInt() + 1);
            }
        }
        System.out.println("OWN: " + (System.currentTimeMillis() - time));

        random = new Random(0xDEADBEEF);
        time = System.currentTimeMillis();
        for (int r=0; r<100; r++) {
            IntOpenHashSet set = new IntOpenHashSet();
            for (int k=0; k<1000000; k++) {
                set.add(random.nextInt() + 1);
            }
        }
        System.out.println("HPPC: " + (System.currentTimeMillis() - time));
        
    }

    /** Seed */
    private final int seed = getSeed();

    /** Number of elements that can be put into table until a rehash occurs */
    private int       threshold;

    /** The entry array. */
    public int[]      buckets;

    /** Current number of elements. */
    public int        size;

    /** The last element added to this set */
    public int        last;

    public SUDA2IntSet() {
        this.buckets = new int[DEFAULT_INITIAL_CAPACITY];
        this.threshold = getThreshold(this.buckets.length);
    }
    
    public void add(int value) {
        size += this.add(this.buckets, value, hashcode(value)) ? 1 : 0;
        if (size == threshold) {
            this.rehash();
        }
    }
   
    public boolean contains(int value) {
        
        final int mask = (buckets.length) - 1;
        final int slot = (hashcode(value) & mask);

        for (int i = slot; i < buckets.length; i ++) {
            if (buckets[i] == value) {
                return false;
            } else if (buckets[i] == 0) {
                buckets[i] = value;
                return true;
            }
        }

        for (int i = 0; i < slot; i ++) {
            if (buckets[i] == value) {
                return false;
            } else if (buckets[i] == 0) {
                buckets[i] = value;
                return true;
            }
        }
        
        throw new IllegalStateException("Element not added");
    }

    private boolean add(int[] buckets, int value, int hash) {
        
        this.last = value;
        final int mask = (buckets.length) - 1;
        final int slot = (hash & mask);

        for (int i = slot; i < buckets.length; i ++) {
            if (buckets[i] == value) {
                return false;
            } else if (buckets[i] == 0) {
                buckets[i] = value;
                return true;
            }
        }

        for (int i = 0; i < slot; i ++) {
            if (buckets[i] == value) {
                return false;
            } else if (buckets[i] == 0) {
                buckets[i] = value;
                return true;
            }
        }
        
        throw new IllegalStateException("Element not added");
    }

    /**
     * Returns a seed for this specific collection
     * @return
     */
    private final int getSeed() {
        long seed = System.nanoTime();
        seed = (seed ^ (seed >>> 32)) * 0x4cd6944c5cc20b6dL;
        seed = (seed ^ (seed >>> 29)) * 0xfc12c5b19d3259e9L;
        return (int) (seed ^ (seed >>> 32));
    }
    
    /**
     * Returns the threshold
     * @param size
     * @return
     */
    private int getThreshold(int size) {
        return (int) Math.ceil(size * DEFAULT_LOAD_FACTOR);
    }
    
    /**
     * Murmur hash
     * @param value
     * @return
     */
    private final int hashcode(int value) {
        value = (value ^ (value >>> 16)) * 0x85ebca6b;
        value = (value ^ (value >>> 13)) * 0xc2b2ae35;
        return (value ^ (value >>> 16)) ^ seed;
    }

    private void rehash() {
        
        int[] _buckets = new int[buckets.length << 1];
        int _threshold = getThreshold(_buckets.length);

        // In reverse order
        for (int i = this.buckets.length - 1; i >= 0; i--) {
            if (buckets[i] != 0) {
                this.add(_buckets, buckets[i], hashcode(buckets[i]));
            }
        }
        
        this.buckets = _buckets;
        this.threshold = _threshold;
    }
}
