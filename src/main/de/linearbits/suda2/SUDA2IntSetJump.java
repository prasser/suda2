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
public class SUDA2IntSetJump extends SUDA2IntSet {

    /** Bucket*/
    private int int0;
    /** Bucket*/
    private int int1;
    /** Bucket*/
    private int int2;
    /** Bucket*/
    private int int3;
    /** Bucket*/
    private int int4;
    /** Bucket*/
    private int int5;
    /** Bucket*/
    private int int6;
    /** Bucket*/
    private int int7;
    
    /** Size*/
    private int size;

    /** Min */
    private int       min  = Integer.MAX_VALUE;
    /** Max */
    private int       max  = Integer.MIN_VALUE;
    
    /**
     * Creates a new instance
     */
    public SUDA2IntSetJump() {
        instance(TYPE_INT_SET_SMALL);
    }
    
    @Override
    public void add(int value) {
        min = Math.min(value, min);
        max = Math.max(value, max);
        switch(size) {
        case 0: int0 = value; break;
        case 1: int1 = value; break;
        case 2: int2 = value; break;
        case 3: int3 = value; break;
        case 4: int4 = value; break;
        case 5: int5 = value; break;
        case 6: int6 = value; break;
        case 7: int7 = value; break;
        }
        size++; // TODO: Hopefully, we never add the same value twice
    }

    @Override
    public boolean contains(int value) {
        boolean result = false;
        switch(size) {
        case 8: result |= (int7 == value);
        case 7: result |= (int6 == value);
        case 6: result |= (int5 == value);
        case 5: result |= (int4 == value);
        case 4: result |= (int3 == value);
        case 3: result |= (int2 == value);
        case 2: result |= (int1 == value);
        case 1: result |= (int0 == value);
        }
        return result;
    }
    
    @Override
    public boolean containsSpecialRow(SUDA2Item[] items, SUDA2Item referenceItem, int[][] data) {
        
        // ----------------------------------------------------- //
        startTiming();
        // ----------------------------------------------------- //
        
        switch(size) {
        case 8: if (containsSpecialRow(items, referenceItem, data[int7 - 1])) { return true; }
        case 7: if (containsSpecialRow(items, referenceItem, data[int6 - 1])) { return true; }
        case 6: if (containsSpecialRow(items, referenceItem, data[int5 - 1])) { return true; }
        case 5: if (containsSpecialRow(items, referenceItem, data[int4 - 1])) { return true; }
        case 4: if (containsSpecialRow(items, referenceItem, data[int3 - 1])) { return true; }
        case 3: if (containsSpecialRow(items, referenceItem, data[int2 - 1])) { return true; }
        case 2: if (containsSpecialRow(items, referenceItem, data[int1 - 1])) { return true; }
        case 1: if (containsSpecialRow(items, referenceItem, data[int0 - 1])) { return true; }
        }
        // ----------------------------------------------------- //
        endTiming(TYPE_INT_SET_SMALL, TYPE_METHOD_SPECIALROW, size);
        // ----------------------------------------------------- //
        return false;
    }

    @Override
    public Type getType() {
        return Type.JUMP;
    }

    @Override
    public SUDA2IntSet intersectWith(SUDA2IntSet other) {

        // No intersection
        if (this.size == 0 || this.max < other.min() || other.max() < this.min) {
            return SUDA2IntSet.EMPTY_SET;
        }
        
        // ----------------------------------------------------- //
        startTiming();
        // ----------------------------------------------------- //

        // Intersect support rows with those provided
        SUDA2IntSetJump result = new SUDA2IntSetJump();
        switch (size) {
        case 8: if (other.contains(int7)) { result.add(int7); }
        case 7: if (other.contains(int6)) { result.add(int6); }
        case 6: if (other.contains(int5)) { result.add(int5); }
        case 5: if (other.contains(int4)) { result.add(int4); }
        case 4: if (other.contains(int3)) { result.add(int3); }
        case 3: if (other.contains(int2)) { result.add(int2); }
        case 2: if (other.contains(int1)) { result.add(int1); }
        case 1: if (other.contains(int0)) { result.add(int0); }
        }
        
        // ----------------------------------------------------- //
        endTiming(TYPE_INT_SET_SMALL, TYPE_METHOD_INTERSECTION, size);
        // ----------------------------------------------------- //

        // Return
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

        int rows = 0;
        switch (size) {
        case 8: rows += other.contains(int7) ? 1 : 0;
        case 7: rows += other.contains(int6) ? 1 : 0;
        case 6: rows += other.contains(int5) ? 1 : 0;
        case 5: rows += other.contains(int4) ? 1 : 0;
        case 4: rows += other.contains(int3) ? 1 : 0;
        case 3: rows += other.contains(int2) ? 1 : 0;
        case 2: rows += other.contains(int1) ? 1 : 0;
        case 1: rows += other.contains(int0) ? 1 : 0;
        }
        // ----------------------------------------------------- //
        endTiming(TYPE_INT_SET_SMALL, TYPE_METHOD_SUPPORTROW, size);
        // ----------------------------------------------------- //
        return rows == 1;
    }

    @Override
    public int max() {
        return max;
    }

    @Override
    public int min() {
        return min;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "Size=" + size + " elements=" + int0 + ", " + int1 + ", " + int2 + ", " + int3 + ", " + int4 + ", " + int5 + ", " + int6 + ", " + int7;
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
}
