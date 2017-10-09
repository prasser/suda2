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
 * A very basic integer set
 * 
 * @author Fabian Prasser
 */
public class SUDA2IntSetSmall2 extends SUDA2IntSet {

    public static int COUNT = 0;
    
    /** Size*/
    public static final int SIZE = 8;

    /** Buckest*/
    private int[] buckets = new int[SIZE];

    /** Min */
    private int       min  = Integer.MAX_VALUE;
    /** Max */
    private int       max  = Integer.MIN_VALUE;
    
    /** Size*/
    private int size;
    
    public SUDA2IntSetSmall2() {
        COUNT++;
    }

    @Override
    public void add(int value) {
        min = Math.min(value, min);
        max = Math.max(value, max);
        buckets[size++] = value; // TODO: Hopefully, we never add the same value twice
    }

    @Override
    public boolean contains(int value) {
        int bound = size; // Range check elimination
        for (int i=0; i < bound; i++) {
            if (buckets[i] == value) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean containsSpecialRow(SUDA2Item[] items, SUDA2Item referenceItem, int[][] data) {
        
        int bound = size; // Range check elimination
        for (int i=0; i < bound; i++) {
            if (containsSpecialRow(items, referenceItem, data[buckets[i] - 1])) { 
                return true;
            }
        }
        return false;
    }

    @Override
    public SUDA2IntSet intersectWith(SUDA2IntSet other) {

        // No intersection
        if (this.max < other.min() || other.max() < this.min) {
            return new SUDA2IntSetSmall();
        }

        // Intersect support rows with those provided
        SUDA2IntSetSmall2 rows = new SUDA2IntSetSmall2();
        int bound = size; // Range check elimination
        for (int i=0; i < bound; i++) {
            int value = buckets[i];
            if (other.contains(value)) { 
                rows.add(value);
            }
        }
        
        // Return
        return rows;
    }

    @Override
    public boolean isSupportRowPresent(SUDA2IntSet other) {

        // No intersection
        if (this.max < other.min() || other.max() < this.min) {
            return false;
        }

        boolean found = false;
        int bound = size; // Range check elimination
        for (int i=0; i < bound; i++) {
            int value = buckets[i];
            if (other.contains(value)) { 
                if (found) {
                    return false;
                } else {
                    found = true;
                }
            }
        }
        return found;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int min() {
        return min;
    }

    @Override
    public int max() {
        return max;
    }
    /**
     * Searches for the special row
     * @param items
     * @param referenceItem
     * @param row
     * @return
     */
    private boolean containsSpecialRow(SUDA2Item[] items, SUDA2Item referenceItem, int[] row) {
        for (SUDA2Item item : items) {
            if (!item.isContained(row)) {
                return false;
            }
        }
        if (referenceItem.isContained(row)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isBitSet() {
        return false;
    }
}