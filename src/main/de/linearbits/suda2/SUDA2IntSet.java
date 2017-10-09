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
public abstract class SUDA2IntSet {

    /**
     * Returns the smallest power of two larger than the given size
     * @param size
     * @return
     */
    private static final int getCapacity(int size) {
        --size;
        size |= size >> 1;
        size |= size >> 2;
        size |= size >> 4;
        size |= size >> 8;
        size |= size >> 16;
        return size + 1;
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
     * Returns the last element added to the set
     * @return
     */
    public abstract int last();
    
    /**
     * Returns the size of the set
     * @return
     */
    public abstract int size();
    
    /**
     * Searches for exactly one support row
     * @param other
     * @return
     */
    public abstract boolean isSupportRowPresent(SUDA2IntSet other);

    /**
     * Returns a new set that contains only elements contained in both sets
     * 
     * @param other
     * @return
     */
    public abstract SUDA2IntSet intersectWith(SUDA2IntSet other);

    /**
     * Returns whether the special row is contained in this set
     * @param items 
     * @param referenceItem
     * @param data
     * @return
     */
    public abstract boolean containsSpecialRow(SUDA2Item[] items, SUDA2Item referenceItem, int[][] data);
}
