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

import java.util.Arrays;

/**
 * A very basic integer set using murmur hashing and linear probing.
 * 
 * @author Fabian Prasser
 */
public class SUDA2IntSetHash extends SUDA2IntSet {

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
    private int[]     buckets;

    /** Current number of elements. */
    private int       size;

    /** Min */
    private int       min  = Integer.MAX_VALUE;

    /** Max */
    private int       max  = Integer.MIN_VALUE;

    /**
     * Creates a new instance
     */
    public SUDA2IntSetHash() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Creates a new instance
     * @param capacity
     */
    public SUDA2IntSetHash(int capacity) {
        this.buckets = new int[DEFAULT_INITIAL_CAPACITY];
        this.threshold = getThreshold(this.buckets.length);
        instance(TYPE_INT_SET_HASH);
    }
    
    @Override
    public void add(int value) {
        min = Math.min(value, min);
        max = Math.max(value, max);
        this.add(this.buckets, value, hashcode(value));
        size++; // TODO: Hopefully, we never add the same value twice
        if (size == threshold) {
            this.rehash();
        }
    }

    @Override
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
    
    @Override
    public boolean containsSpecialRow(SUDA2Item[] items, SUDA2Item referenceItem, int[][] data) {
        
        // ----------------------------------------------------- //
        startTiming();
        // ----------------------------------------------------- //
        
        final int length = buckets.length;
        final int numItems = items.length;
        outer: for (int i = 0; i < length; i++) {
            if (buckets[i] != 0) {
                int[] row = data[buckets[i] - 1];
                for (int j=0; j<numItems; j++) {
                    if (!items[j].isContained(row)) {
                        continue outer;
                    }
                }
                if (referenceItem.isContained(row)) {
                    continue;
                }
                // ----------------------------------------------------- //
                endTiming(TYPE_INT_SET_HASH, TYPE_METHOD_SPECIALROW, size);
                // ----------------------------------------------------- //
                return true;
            }
        }
        // ----------------------------------------------------- //
        endTiming(TYPE_INT_SET_HASH, TYPE_METHOD_SPECIALROW, size);
        // ----------------------------------------------------- //
        return false;
    }
    
    @Override
    public SUDA2IntSet intersectWith(SUDA2IntSet other) {

        // No output, empty set
        if (size == 0 || this.max < other.min() || other.max() < this.min) {
            return new SUDA2IntSetJump();
        }

        // ----------------------------------------------------- //
        startTiming();
        // ----------------------------------------------------- //
        
        // Prepare
        SUDA2IntSet result = null;
        
        // Intersect ranges
        int min = Math.max(this.min,  other.min());
        int max = Math.min(this.max,  other.max());
        
        
        // Very small set
        if (size <= 8) {
            result = new SUDA2IntSetJump();
            
        // Hash or bitset
        } else {
            
            // Estimate size assuming uniform distribution
            int estimatedSize = (int)((double)size * (double)(max - min) / (double)(this.max - this.min)) + 1;
    
            // Calculate capacity needed for hash set of estimates size
            int capacity = estimatedSize - 1;
            capacity |= capacity >> 1;
            capacity |= capacity >> 2;
            capacity |= capacity >> 4;
            capacity |= capacity >> 8;
            capacity |= capacity >> 16;
            capacity++;
            
            // If it saves space, use a bit set
            if ((capacity << 5) >= max - min) {
                result = new SUDA2IntSetBits(min, max);
            } else {
                // Fall back to hash set
                result = new SUDA2IntSetHash(capacity);                
            }
        }

        // Intersect support rows with those provided
        for (int i = 0; i < buckets.length; i++) {
            int row = buckets[i];
            if (row != 0 && row >= min && row <= max && other.contains(row)) {
                result.add(row);
            }
        }

        // ----------------------------------------------------- //
        endTiming(TYPE_INT_SET_HASH, TYPE_METHOD_INTERSECTION, size);
        // ----------------------------------------------------- //

        return result;
    }

    @Override
    public boolean isSupportRowPresent(SUDA2IntSet other) {

        // No intersection
        if (this.max < other.min() || other.max() < this.min) {
            return false;
        }
        
        // ----------------------------------------------------- //
        startTiming();
        // ----------------------------------------------------- //

        // Intersect ranges
        int min = Math.max(this.min,  other.min());
        int max = Math.min(this.max,  other.max());
        
        boolean supportRowFound = false;
        for (int i = 0; i < buckets.length; i++) {
            int row = buckets[i];
            if (row != 0 && row >= min && row <= max && other.contains(row)) {
                if (supportRowFound) {
                    // ----------------------------------------------------- //
                    endTiming(TYPE_INT_SET_HASH, TYPE_METHOD_SUPPORTROW, size);
                    // ----------------------------------------------------- //
                    // More than one support row
                    return false;
                } else {
                    supportRowFound = true;
                }
            }
        }
        // ----------------------------------------------------- //
        endTiming(TYPE_INT_SET_HASH, TYPE_METHOD_SUPPORTROW, size);
        // ----------------------------------------------------- //
        return supportRowFound;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Adds a value to the set
     * @param buckets
     * @param value
     * @param hash
     * @return
     */
    private void add(int[] buckets, int value, int hash) {
        
        final int mask = buckets.length - 1;
        final int slot = hash & mask;
        
        for (int i = slot; i < buckets.length; i ++) {
            if (buckets[i] == value || buckets[i] == 0) {
                buckets[i] = value;
                return;
            }
        }

        for (int i = 0; i < slot; i ++) {
            if (buckets[i] == value || buckets[i] == 0) {
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

    @Override
    public int min() {
        return min;
    }

    @Override
    public int max() {
        return max;
    }

    @Override
    public boolean isBitSet() {
        return false;
    }

    @Override
    public String toString() {
        return "Size=" + size + " array=" + Arrays.toString(buckets);
    }
}
