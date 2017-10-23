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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A set of items
 * 
 * @author Fabian Prasser
 */
public class SUDA2ItemRegistry {


    /** Default */
    private static final float DEFAULT_LOAD_FACTOR      = 0.75f;

    /** Default */
    private static final int   DEFAULT_INITIAL_CAPACITY = 8;

    /**
     * Returns the threshold
     * @param size
     * @return
     */
    private static final int getThreshold(int size) {
        return (int) Math.ceil(size * DEFAULT_LOAD_FACTOR);
    }

    /** Number of elements that can be put into table until a rehash occurs */
    private int        threshold;

    /** The entry array. */
    public SUDA2Item[] values;

    /** Current number of elements. */
    public int         size;
   
    /**
     * Creates a new instance
     */
    public SUDA2ItemRegistry() {
        this.values = new SUDA2Item[DEFAULT_INITIAL_CAPACITY];
        this.threshold = getThreshold(this.values.length);
    }

    /**
     * Adds an item to this index
     * @param item
     */
    public void add(SUDA2Item item) {
        
        // Prepare
        long key = item.getId();
        int mask = values.length - 1;
        int slot = hashcode(key) & mask;

        // Add
        while (values[slot] != null) {
            slot = (slot + 1) & mask;
        }
        values[slot] = item;
        
        // Rehash
        if (++size == threshold) {
            this.rehash();
        }
    }
    
    /**
     * Returns the item for a given key
     * @param key
     * @return
     */
    public SUDA2Item get(long key) {

        // Prepare
        int mask = values.length - 1;
        int slot = hashcode(key) & mask;

        // Add
        while (values[slot] != null && values[slot].getId() != key) {
            slot = (slot + 1) & mask;
        }
        return values[slot];
    }

    /**
     * Returns a list containing items sorted by rank
     * @param items
     * @return
     */
    public List<SUDA2Item> getSortedItemList() {

        // Create list and sort by support
        List<SUDA2Item> list = new ArrayList<SUDA2Item>();
        for (SUDA2Item item : values) {
            if (item != null) {
                list.add(item);
            }
        }
        Collections.sort(list, new Comparator<SUDA2Item>() {
            @Override
            public int compare(SUDA2Item o1, SUDA2Item o2) {
                return o1.getSupport() < o2.getSupport() ? -1 :
                       o1.getSupport() > o2.getSupport() ? +1 : 0;
            }
        });
        
        // Return
        return list;
    }
    
    /**
     * Either returns an existing entry or creates a new one
     * @param column
     * @param value
     * @return
     */
    public SUDA2Item register(int column, int value) {

        // Prepare
        long key = SUDA2Item.getId(column, value);
        int mask = values.length - 1;
        int slot = hashcode(key) & mask;

        // Search
        while (values[slot] != null && values[slot].getId() != key) {
            slot = (slot + 1) & mask;
        }
        
        // Add
        if (values[slot] == null) {
            values[slot] = new SUDA2Item(column, value, key);        
        }
        
        // Store
        SUDA2Item result =  values[slot];

        // Rehash
        if (++size == threshold) {
            this.rehash();
        }
        
        // Return
        return result;
    }
    
    /**
     * Murmur hash
     * @param value
     * @return
     */
    private int hashcode(long value) {
        value = (value ^ (value >>> 32)) * 0x4cd6944c5cc20b6dL;
        value = (value ^ (value >>> 29)) * 0xfc12c5b19d3259e9L;
        return (int)(value ^ (value >>> 32));
    }

    /**
     * Rehashes the map
     */
    private void rehash() {
        
        // Prepare
        SUDA2Item[] _values = new SUDA2Item[values.length << 1];
        int _threshold = getThreshold(_values.length);
        int _mask = _values.length - 1;

        // In reverse order
        for (int i = this.values.length - 1; i >= 0; i--) {
            if (this.values[i] != null) {

                // Add                
                long key = values[i].getId();
                int slot = hashcode(key) & _mask;
                while (_values[slot] != null) {
                    slot = (slot + 1) & _mask;
                }
                _values[slot] = values[i];
                
            }
        }
        
        this.values = _values;
        this.threshold = _threshold;
    }
}