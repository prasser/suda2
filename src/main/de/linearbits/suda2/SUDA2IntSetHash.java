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
    private int[]      buckets;

    /** Current number of elements. */
    private int        size;
    
    /** The last element added to this set */
    private int        last;
   
    /**
     * Creates a new instance
     */
    public SUDA2IntSetHash() {
        this.buckets = new int[DEFAULT_INITIAL_CAPACITY];
        this.threshold = getThreshold(this.buckets.length);
    }

    /* (non-Javadoc)
     * @see de.linearbits.suda2.SUDA2IntSet#add(int)
     */
    @Override
    public void add(int value) {
        size += this.add(this.buckets, value, hashcode(value)) ? 1 : 0;
        if (size == threshold) {
            this.rehash();
        }
    }

    /* (non-Javadoc)
     * @see de.linearbits.suda2.SUDA2IntSet#contains(int)
     */
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
        outer: for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != 0) {
                int[] row = data[buckets[i] - 1];
                for (SUDA2Item item : items) {
                    if (!item.isContained(row)) {
                        continue outer;
                    }
                }
                if (referenceItem.isContained(row)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public SUDA2IntSet intersectWith(SUDA2IntSet other) {

        // Intersect support rows with those provided
        SUDA2IntSetHash rows = new SUDA2IntSetHash();
        for (int i = 0; i < buckets.length; i++) {
            int row = buckets[i];
            if (row != 0 && other.contains(row)) {
                rows.add(row);
            }
        }
        
        // We pack the set, if necessary
        if (rows.size() <= SUDA2IntSetSmall.SIZE) {
            SUDA2IntSetSmall small = new SUDA2IntSetSmall();
            for (int i = 0; i < rows.buckets.length; i++) {
                int row = rows.buckets[i];
                if (row != 0) {
                    small.add(row);
                }
            }
            return small;
        } else {
            return rows;
        }
    }
    
    @Override
    public boolean isSupportRowPresent(SUDA2IntSet other) {

        boolean supportRowFound = false;
        for (int i = 0; i < buckets.length; i++) {
            int row = buckets[i];
            if (row != 0 && other.contains(row)) {
                if (supportRowFound) {
                    // More than one support row
                    return false;
                } else {
                    supportRowFound = true;
                }
            }
        }
        return supportRowFound;
    }

    @Override
    public int last() {
        return last;
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
    private boolean add(int[] buckets, int value, int hash) {
        
        this.last = value;
        final int mask = buckets.length - 1;
        final int slot = hash & mask;
        
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
