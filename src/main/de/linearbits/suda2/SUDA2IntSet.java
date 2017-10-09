/*
 * SUDA2: An implementation of the SUDA2 algorithm for Java
 * Copyright 2017 Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.linearbits.suda2;

/**
 * A very basic integer set using murmur hashing and linear probing.
 * 
 * @author Fabian Prasser
 */
public class SUDA2IntSet {
    
    /** Default */
    private static final float DEFAULT_LOAD_FACTOR      = 0.75f;

    /** Default */
    private static final int   DEFAULT_INITIAL_CAPACITY = 8;

    /**
     * Returns a seed for this specific collection
     * @return
     */
    private static final int getSeed() {
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
    private static final int getThreshold(int size) {
        return (int) Math.ceil(size * DEFAULT_LOAD_FACTOR);
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
   
    /**
     * Creates a new instance
     */
    public SUDA2IntSet() {
        this.buckets = new int[DEFAULT_INITIAL_CAPACITY];
        this.threshold = getThreshold(this.buckets.length);
    }

    /**
     * Adds a new value to the set
     * @param value
     */
    public void add(int value) {
        this.add(this.buckets, value, hashcode(value));
        size++; // We never add the same value more than once
        if (size == threshold) {
            this.rehash();
        }
    }

    /**
     * Returns whether the given value is contained
     * @param value
     * @return
     */
    public boolean contains(int value) {
        
        final int mask = buckets.length - 1;
        final int slot = hashcode(value) & mask;

        for (int i = slot; i < buckets.length; i ++) {
            if (buckets[i] == value) {
                return true;
            } else if (buckets[i] == 0) {
                return false;
            }
        }

        for (int i = 0; i < slot; i ++) {
            if (buckets[i] == value) {
                return true;
            } else if (buckets[i] == 0) {
                return false;
            }
        }
        
        throw new IllegalStateException("Illegal state. This should not happen.");
    }
    
    /**
     * Adds a value to the set
     * @param buckets
     * @param value
     * @param hash
     * @return
     */
    private void add(int[] buckets, int value, int hash) {
        
        this.last = value;
        final int mask = buckets.length - 1;
        final int slot = hash & mask;
        
        for (int i = slot; i < buckets.length; i ++) {
            if (buckets[i] == value) {
                return;
            } else if (buckets[i] == 0) {
                buckets[i] = value;
                return;
            }
        }

        for (int i = 0; i < slot; i ++) {
            if (buckets[i] == value) {
                return;
            } else if (buckets[i] == 0) {
                buckets[i] = value;
                return;
            }
        }
        
        throw new IllegalStateException("Illegal state. This should not happen.");
    }
    
    /**
     * Murmur hash
     * @param value
     * @return
     */
    private int hashcode(int value) {
        value = value ^ seed;
        value = (value ^ (value >>> 16)) * 0x85ebca6b;
        value = (value ^ (value >>> 13)) * 0xc2b2ae35;
        return (value ^ (value >>> 16));
    }

    /**
     * Rehashes the set
     */
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
