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
 * A very basic map from long identifiers to items
 * 
 * @author Fabian Prasser
 */
public class SUDA2ItemIndex {
    
    /** Default */
    private static final float DEFAULT_LOAD_FACTOR      = 0.75f;

    /** Default */
    private static final int   DEFAULT_INITIAL_CAPACITY = 8;

    /**
     * Returns a seed for this specific collection
     * @return
     */
    private static final long getSeed() {
        long seed = System.nanoTime();
        seed = (seed ^ (seed >>> 32)) * 0x4cd6944c5cc20b6dL;
        seed = (seed ^ (seed >>> 29)) * 0xfc12c5b19d3259e9L;
        return (seed ^ (seed >>> 32));
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
    private final long seed = getSeed();

    /** Number of elements that can be put into table until a rehash occurs */
    private int        threshold;

    /** The entry array. */
    public long[]      keys;

    /** The entry array. */
    public SUDA2Item[] values;

    /** Current number of elements. */
    public int         size;
   
    /**
     * Creates a new instance
     */
    public SUDA2ItemIndex() {
        this.keys = new long[DEFAULT_INITIAL_CAPACITY];
        this.values = new SUDA2Item[DEFAULT_INITIAL_CAPACITY];
        this.threshold = getThreshold(this.keys.length);
    }

    public SUDA2Item get(long key) {

        final int mask = keys.length - 1;
        final int slot = hashcode(key) & mask;
        
        for (int i = slot; i < keys.length; i ++) {
            if (values[i] != null && keys[i] == key) {
                return values[i];
            } else if (values[i] == null) {
                return null;
            }
        }

        for (int i = 0; i < slot; i ++) {
            if (values[i] != null && keys[i] == key) {
                return values[i];
            } else if (values[i] == null) {
                return null;
            }
        }
        
        throw new IllegalStateException("Illegal state. This should not happen.");
    }
    
    /**
     * If the key is found, we return the slot index, else -(slot index + 1)
     * @param key
     * @return
     */
    public int getSlot(long key) {

        final int mask = keys.length - 1;
        final int slot = hashcode(key) & mask;
        
        for (int i = slot; i < keys.length; i ++) {
            if (values[i] != null && keys[i] == key) {
                return i;
            } else if (values[i] == null) {
                return -(i + 1);
            }
        }

        for (int i = 0; i < slot; i ++) {
            if (values[i] != null && keys[i] == key) {
                return i;
            } else if (values[i] == null) {
                return -(i + 1);
            }
        }
        
        throw new IllegalStateException("Illegal state. This should not happen.");
    }
    
    /**
     * Adds a new value to the set
     * @param key
     * @param value
     */
    public void put(long key, SUDA2Item value) {
        size += this.put(this.keys, this.values, key, hashcode(key), value) ? 1 : 0;
        if (size == threshold) {
            this.rehash();
        }
    }

    /**
     * Inserts a value into the given slot
     * @param key
     * @param slot
     * @param value
     */
    public void putSlot(long key, int slot, SUDA2Item value) {
        this.keys[slot] = key;
        this.values[slot] = value;
        size ++;
        if (size == threshold) {
            this.rehash();
        }
    }

    /**
     * Murmur hash
     * @param value
     * @return
     */
    private int hashcode(long value) {
        value = value ^ seed;
        value = (value ^ (value >>> 32)) * 0x4cd6944c5cc20b6dL;
        value = (value ^ (value >>> 29)) * 0xfc12c5b19d3259e9L;
        return (int)(value ^ (value >>> 32));
    }

    /**
     * Adds an element to the map
     * @param keys
     * @param values
     * @param key
     * @param hash
     * @param value
     * @return
     */
    private boolean put(long[] keys, SUDA2Item[] values, long key, int hash, SUDA2Item value) {
        
        final int mask = keys.length - 1;
        final int slot = hash & mask;
        
        for (int i = slot; i < keys.length; i ++) {
            if (values[i] != null && keys[i] == key) {
                return false;
            } else if (values[i] == null) {
                values[i] = value;
                keys[i] = key;
                return true;
            }
        }

        for (int i = 0; i < slot; i ++) {
            if (values[i] != null && keys[i] == key) {
                return false;
            } else if (values[i] == null) {
                values[i] = value;
                keys[i] = key;
                return true;
            }
        }
        
        throw new IllegalStateException("Illegal state. This should not happen.");
    }

    /**
     * Rehashes the set
     */
    private void rehash() {
        
        long[] _keys = new long[keys.length << 1];
        SUDA2Item[] _values = new SUDA2Item[keys.length << 1];
        int _threshold = getThreshold(_keys.length);

        // In reverse order
        for (int i = this.keys.length - 1; i >= 0; i--) {
            if (this.values[i] != null) {
                this.put(_keys, _values, keys[i], hashcode(keys[i]), values[i]);
            }
        }
        
        this.keys = _keys;
        this.values = _values;
        this.threshold = _threshold;
    }
}
