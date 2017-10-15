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
 * Abstract int set
 * 
 * @author Fabian Prasser
 */
public abstract class SUDA2IntSet extends Timeable {
    
    /**
     * The different types of sets available
     * 
     * @author Fabian Prasser
     */
    public static enum Type {
        HASH,
        BITS,
        JUMP,
        EMPTY
    }
    
    /** Empty set */
    public static SUDA2IntSet EMPTY_SET = new SUDA2IntSetEmpty();
    
    /**
     * Tries to guess the best type of set to use for output data for a specific intersection operation
     * @param size
     * @param min1
     * @param max1
     * @param min2
     * @param max2
     * @return
     */
    public static SUDA2IntSet getResultSet(int size, int min1, int max1, int min2, int max2) {

        // No output, empty set
        if (size == 0 || max1 < min2 || max2 < min1) {
            return EMPTY_SET;
        }
            
        // Intersect
        int overlapMin = Math.max(min1,  min2);
        int overlapMax = Math.min(max1,  max2);
        
        
        // Very small set
        if (size <= 8) {
            return new SUDA2IntSetJump();
        }
        
        // Estimate size assuming uniform distribution
        int estimatedSize = (int)((double)size * (double)(overlapMax - overlapMin) / (double)(max1 - min1));

        // Calculate capacity needed for hash set of estimates size
        int capacity = estimatedSize - 1;
        capacity |= capacity >> 1;
        capacity |= capacity >> 2;
        capacity |= capacity >> 4;
        capacity |= capacity >> 8;
        capacity |= capacity >> 16;
        capacity++;
        
        // If it saves space, use a bit set
        if ((capacity << 5) >= overlapMax - overlapMin) {
            return new SUDA2IntSetBits(overlapMin, overlapMax);
        }
        
        // Fall back to hash set
        return new SUDA2IntSetHash(capacity);
    }

    /**
     * Adds a new value to the set
     * @param value
     */
    public abstract void add(int value);

    /**
     * Returns whether the given value is contained
     * @param value
     * @return
     */
    public abstract boolean contains(int value);

    /**
     * Returns whether the special row is contained in this set
     * @param items 
     * @param referenceItem
     * @param data
     * @return
     */
    public abstract boolean containsSpecialRow(SUDA2Item[] items, SUDA2Item referenceItem, int[][] data);
    
    /**
     * Returns whether the set is a bit set
     * @return
     */
    public abstract Type getType();
    
    /**
     * Returns a new set that contains only elements contained in both sets
     * 
     * @param other
     * @return
     */
    public abstract SUDA2IntSet intersectWith(SUDA2IntSet other);
    
    /**
     * Searches for exactly one support row
     * @param other
     * @return
     */
    public abstract boolean isSupportRowPresent(SUDA2IntSet other);

    /**
     * Return min
     * @return
     */
    public abstract int max();

    /**
     * Return max
     * @return
     */
    public abstract int min();
    
    /**
     * Returns the size of the set
     * @return
     */
    public abstract int size();
    
    /**
     * To string
     */
    public abstract String toString();
}
