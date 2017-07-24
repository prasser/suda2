package de.linearbits.suda2;

import java.util.Random;

import com.carrotsearch.hppc.IntOpenHashSet;

public class SUDA2IntSet {
    
    /** Default */
    private static final float DEFAULT_LOAD_FACTOR      = 0.75f;

    /** Default */
    private static final int   DEFAULT_INITIAL_CAPACITY = 8;

    /** Seed */
    private final int          seed                     = getSeed();

    /** The entry array. */
    private int[]              buckets;

    /** Current number of elements. */
    private int                elements;

    /** Number of elements that can be put into table until a rehash occurs */
    private int                threshold;

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
     * Murmur hash
     * @param value
     * @return
     */
    private final int hashcode(int value) {
        value = (value ^ (value >>> 16)) * 0x85ebca6b;
        value = (value ^ (value >>> 13)) * 0xc2b2ae35;
        return (value ^ (value >>> 16)) ^ seed;
    }
    
    public SUDA2IntSet() {
        this.buckets = new int[DEFAULT_INITIAL_CAPACITY << 1];
        this.threshold = getThreshold(this.buckets.length >> 1);
    }
   
    /**
     * Returns the threshold
     * @param size
     * @return
     */
    private int getThreshold(int size) {
        return (int) Math.ceil(size * DEFAULT_LOAD_FACTOR);
    }

    private boolean add(int[] buckets, int value, int hash) {
        
        final int mask = (buckets.length >> 1) - 1;
        final int slot = (hash & mask) << 1;

        for (int i = slot; i < buckets.length; i += 2) {
            if (buckets[i] == value) {
                return false;
            } else if (buckets[i] == 0) {
                buckets[i] = value;
                buckets[i + 1] = hash;
                return true;
            }
        }

        for (int i = 0; i < slot; i += 2) {
            if (buckets[i] == value) {
                return false;
            } else if (buckets[i] == 0) {
                buckets[i] = value;
                buckets[i + 1] = hash;
                return true;
            }
        }
        
        throw new IllegalStateException("Element not added");
    }
    
    public void add(int value) {
        elements += this.add(this.buckets, value, hashcode(value)) ? 1 : 0;
        if (elements == threshold) {
            this.rehash();
        }
    }
    
    private void rehash() {
        
        int[] _buckets = new int[buckets.length << 1];
        int _threshold = getThreshold(_buckets.length >> 1);

        // In reverse order
        for (int i=this.buckets.length - 2; i>=0; i-=2) {
            if (buckets[i] != 0) {
                this.add(_buckets, buckets[i], buckets[i+1]);
            }
        }
        
        this.buckets = _buckets;
        this.threshold = _threshold;
    }

    public static void main(String[] args) {
        
        Random random = new Random(0xDEADBEEF);
        long time = System.currentTimeMillis();
        for (int r=0; r<100000; r++) {
            SUDA2IntSet set = new SUDA2IntSet();
            for (int k=0; k<1000; k++) {
                set.add(random.nextInt() + 1);
            }
        }
        System.out.println("OWN: " + (System.currentTimeMillis() - time));

        random = new Random(0xDEADBEEF);
        time = System.currentTimeMillis();
        for (int r=0; r<100000; r++) {
            IntOpenHashSet set = new IntOpenHashSet();
            for (int k=0; k<1000; k++) {
                set.add(random.nextInt() + 1);
            }
        }
        System.out.println("HPPC: " + (System.currentTimeMillis() - time));
        
    }
}
