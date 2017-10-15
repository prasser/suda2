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
     * Returns a new set that contains only elements contained in both sets
     * 
     * @param other
     * @return
     */
    public abstract SUDA2IntSet intersectWith(SUDA2IntSet other);
    
    /**
     * Returns whether the set is a bit set
     * @return
     */
    public abstract boolean isBitSet();
    
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
