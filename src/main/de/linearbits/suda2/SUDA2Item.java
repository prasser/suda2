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
 * Each itemset is a concrete value for a concrete attribute
 * 
 * @author Fabian Prasser
 */
public class SUDA2Item {

    /**
     * Packs column and value into a long to be used as a key
     * @param column
     * @param value
     * @return
     */
    public static long getId(int column, int value) {
        return ((long)column) << 32 | ((long)value) & 0xFFFFFFFFL;
    }

    /** Column */
    private final int         column;
    /** Unique id */
    private final long        id;
    /** Value */
    private final int         value;
    /** Support rows */
    private final SUDA2IntSet rows;

    /**
     * Creates a new item
     * @param column
     * @param value
     * @param id
     */
    public SUDA2Item(int column, int value, long id) {
        this.column = column;
        this.value = value;
        this.id = id;
        this.rows = new SUDA2IntSet();
    }
    
    /**
     * Clone constructor
     * @param column
     * @param value
     * @param id
     * @param rows
     */
    SUDA2Item(int column, int value, long id, SUDA2IntSet rows) {
        this.column = column;
        this.value = value;
        this.id = id;
        this.rows = rows;
    }

    /**
     * Adds a row
     * @param row
     */
    public void addRow(int row) {
        this.rows.add(row);
    }

    @Override
    public boolean equals(Object obj) {
        // Just for debugging purposes
        SUDA2Item other = (SUDA2Item) obj;
        if (column != other.column) return false;
        if (value != other.value) return false;
        return true;
    }

    /**
     * Returns this item if it becomes a 1-MSU in the given set of rows,
     * null otherwise
     * @param otherRows
     * @return
     */
    public SUDA2Item get1MSU(SUDA2IntSet otherRows) {
        
        // Range intersection
        if (this.rows.max < otherRows.min || otherRows.max < this.rows.min) {
            return null;
        }

        // Smaller set is rows1
        int size1 = this.rows.size;
        int size2 = otherRows.size;
        SUDA2IntSet rows1 = size1 < size2 ? this.rows : otherRows;
        SUDA2IntSet rows2 = size1 < size2 ? otherRows : this.rows;
        
        // Check if they intersect with exactly one support row
        boolean supportRowFound = false;
        int[] buckets = rows1.buckets;
        for (int i = 0; i < buckets.length; i++) {
            int row = buckets[i];
            if (row != 0 && rows2.contains(row)) {
                // More than one support row
                if (supportRowFound) {
                    return null;
                } else {
                    supportRowFound = true;
                }
            }
        }
        
        // Check whether the item is a 1-MSU
        return supportRowFound ? this : null;
    }

    /**
     * Returns the column
     * @return
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the id
     * @return
     */
    public long getId() {
        return id;
    }
    
    /**
     * Returns an instance of this item projected to the given rows
     * @param otherRows
     * @return
     */
    public SUDA2Item getProjection(SUDA2IntSet otherRows) {

        // Range intersection
        if (this.rows.max < otherRows.min || otherRows.max < this.rows.min) {
            return null;
        }

        // Range elimination
        int max = Math.min(this.rows.max, otherRows.max);
        int min = Math.max(this.rows.min, otherRows.min);
        
        // Smaller set is set 1
        int size1 = this.rows.size;
        int size2 = otherRows.size;
        SUDA2IntSet rows1 = size1 < size2 ? this.rows : otherRows;
        SUDA2IntSet rows2 = size1 < size2 ? otherRows : this.rows;
        
        // Intersect support rows with those provided
        SUDA2IntSet rows = new SUDA2IntSet();
        final int[] buckets = rows1.buckets;
        for (int i = 0; i < buckets.length; i++) {
            int row = buckets[i];
            if (row != 0 && row >= min && row <= max && rows2.contains(row)) {
                rows.add(row);
            }
        }
        
        // Return
        return rows.size == 0 ? null : new SUDA2Item(this.column, this.value, this.id, rows);
    }

    /**
     * Returns the rows in which this item is located
     * @return
     */
    public SUDA2IntSet getRows() {
        return this.rows;
    }

    /**
     * Returns the support
     * @return
     */
    public int getSupport() {
        return this.rows.size;
    }

    /**
     * Returns the value
     * @return
     */
    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        // Just for debugging purposes
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + value;
        return result;
    }

    /**
     * Returns whether the item is contained in a given row
     * @param row
     * @return
     */
    public boolean isContained(int[] row) {
        return row[column] == value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(column).append(",").append(value).append(")");
        return builder.toString();
    }
}
