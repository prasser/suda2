/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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
 * Calculates statistics needed to compute SUDA DIS scores.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class SUDA2Groupify {

    /**
     * Implements an equivalence class.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    private static final class Entry {

        /** The number of elements in this class. Excluding elements from the public table. */
        public int         count = 0;

        /** The hashcode of this class. */
        public final int   hashcode;

        /** The key of this class. */
        public final int[] key;

        /** The next element in this bucket. */
        public Entry       next  = null;

        /**
         * Creates a new entry.
         * 
         * @param key the key
         * @param hash the hash
         */
        public Entry(final int[] key, final int hash) {
            this.hashcode = hash;
            this.key = key;
        }
    }

    /**
     * Calculates a new capacity.
     * 
     * @param x
     *            the parameter
     * @return the capacity
     */
    private static int calculateCapacity(int x) {
        if (x >= (1 << 30)) { return 1 << 30; }
        if (x == 0) { return 16; }
        x = x - 1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }

    /**
     * Computes the threshold for rehashing.
     * 
     * @param buckets
     * @param loadFactor
     * @return
     */
    private static int calculateThreshold(int buckets, float loadFactor) {
        return (int) (buckets * loadFactor);
    }

    /**
     * Equality check for integer arrays.
     * 
     * @param first an array
     * @param second another array
     * @return true, if equal
     */
    private static boolean equals(int[] first, int[] second) {

        // Make sure that this method can be inlined by keeping
        // its size under 325 bytes
        switch (first.length) {
        case 17:
            if (first[16] != second[16]) { return false; }
        case 16:
            if (first[15] != second[15]) { return false; }
        case 15:
            if (first[14] != second[14]) { return false; }
        case 14:
            if (first[13] != second[13]) { return false; }
        case 13:
            if (first[12] != second[12]) { return false; }
        case 12:
            if (first[11] != second[11]) { return false; }
        case 11:
            if (first[10] != second[10]) { return false; }
        case 10:
            if (first[9] != second[9]) { return false; }
        case 9:
            if (first[8] != second[8]) { return false; }
        case 8:
            if (first[7] != second[7]) { return false; }
        case 7:
            if (first[6] != second[6]) { return false; }
        case 6:
            if (first[5] != second[5]) { return false; }
        case 5:
            if (first[4] != second[4]) { return false; }
        case 4:
            if (first[3] != second[3]) { return false; }
        case 3:
            if (first[2] != second[2]) { return false; }
        case 2:
            if (first[1] != second[1]) { return false; }
        case 1:
            if (first[0] != second[0]) { return false; }
            break;
        default:
            for (int i = 0; i < first.length; i++) {
                if (first[i] != second[i]) { return false; }
            }
        }
        return true;
    }

    /**
     * Computes a hashcode for an integer array, partially unrolled.
     * 
     * @param array
     * @return the hashcode
     */
    private static int hashcode(int[] array) {
        final int len = array.length;
        int result = 23;
        int i = 0;
        // Do blocks of four ints unrolled.
        for (; (i + 3) < len; i += 4) {
            result = (1874161 * result) + // 37 * 37 * 37 * 37
                     (50653 * array[i]) + // 37 * 37 * 37
                     (1369 * array[i + 1]) + // 37 * 37
                     (37 * array[i + 2]) + array[i + 3];
        }
        // Do the rest
        for (; i < len; i++) {
            result = (37 * result) + array[i];
        }
        return result;
    }

    /** Load factor. */
    private final float loadFactor = 0.75f;
    
    /** The entry array. */
    private Entry[]     buckets;

    /** Current number of elements. */
    private int         elements;

    /** Number of elements that can be put into table until a rehash occurs */
    private int         threshold;

    /** Unique records */
    private int         numClassesOfSize1;

    /** Duplicate records */
    private int         numClassesOfSize2;

    /**
     * Constructs a new instance
     *
     * @param capacity The capacity
     */
    public SUDA2Groupify(int capacity) {
        // Initialize
        this.elements = 0;
        this.buckets = new Entry[calculateCapacity(capacity)];
        this.threshold = calculateThreshold(buckets.length, loadFactor);
    }
    
    /**
     * Adds a record
     * @param record
     */
    public void add(int[] record) {

        // Find or create entry
        int hash = hashcode(record);
        int index = hash & (buckets.length - 1);
        Entry entry = findEntry(record, index, hash);
        if (entry == null) {
            if (++elements > threshold) {
                rehash();
                index = hash & (buckets.length - 1);
            }
            entry = createEntry(record, index, hash);
        }
        
        // Track counts
        entry.count++;
        numClassesOfSize1 += entry.count == 1 ? 1 : 0;
        numClassesOfSize1 -= entry.count == 2 ? 1 : 0;
        numClassesOfSize2 += entry.count == 2 ? 1 : 0;
        numClassesOfSize2 -= entry.count == 3 ? 1 : 0;
    }
    
    /**
     * Returns the number of records in equivalence classes of size 2
     * @return
     */
    public int getNumDuplicateRecords() {
        return this.numClassesOfSize2 * 2;
    }
        
    /**
     * Returns the number of records in equivalence classes of size 1
     * @return
     */
    public int getNumUniqueRecords() {
        return this.numClassesOfSize1;
    }
     
    /**
     * Creates a new entry.
     * 
     * @param key the key
     * @param index the index
     * @param hash the hash
     */
    private Entry createEntry(int[] key, int index, int hash) {
        Entry entry = new Entry(key, hash);
        entry.next = buckets[index];
        buckets[index] = entry;
        return entry;
    }
    
    /**
     * Returns the according entry.
     * 
     * @param key the key
     * @param index the index
     * @param keyHash the key hash
     */
    private Entry findEntry(int[] key, int index, int keyHash) {
        Entry m = buckets[index];
        while ((m != null) && ((m.hashcode != keyHash) || !equals(key, m.key))) {
            m = m.next;
        }
        return m;
    }
    
    /**
     * Rehashes this operator.
     */
    private void rehash() {
        int length = calculateCapacity((buckets.length == 0 ? 1 : buckets.length << 1));
        Entry[] newData = new Entry[length];
        for (Entry entry : buckets) {
            while (entry != null) {
                final int index = entry.hashcode & (length - 1);
                entry.next = newData[index];
                newData[index] = entry;
                entry = entry.next;
            }
        }
        buckets = newData;
        threshold = calculateThreshold(buckets.length, loadFactor);
    }
}
