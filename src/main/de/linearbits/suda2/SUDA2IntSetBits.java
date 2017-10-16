package de.linearbits.suda2;

import java.util.Arrays;

/**
 * A set of rows.
 *
 * @author Fabian Prasser
 */
public class SUDA2IntSetBits extends SUDA2IntSet {
   
    /** Array */
    private final long[]     array;

    /** Offset */
    private final int        offset;

    /** Number of bits set */
    private int              size;

    /** Min */
    private int              min                   = Integer.MAX_VALUE;

    /** Max */
    private int              max                   = Integer.MIN_VALUE;
    
    /**
     * Creates a new instance
     *
     * @param min
     * @param max
     */
    public SUDA2IntSetBits(int min, int max) {
        
        this.offset = min & (~0x3f); // Multiple of 64 less than or equal to min
        this.array = new long[(int) (Math.ceil((double) (max - offset + 1) / 64d))];
        instance(TYPE_INT_SET_BITS);
    }

    @Override
    public void add(int value) {
        min = Math.min(value, min);
        max = Math.max(value, max);
        value -= offset;
        int offset = value >> 6; // Divide by 64
        this.array[offset] |= 1L << (value & 63); // x % y = (x & (y - 1))
        this.size ++; // TODO: Hopefully, we never add the same value twice
    }
    

    @Override
    public boolean contains(int value) {
        value -= this.offset;
        int offset = value >> 6; // Divide by 64
        return (value < 0 || offset >= array.length) ? false : ((array[offset] & (1L << (value & 63))) != 0); // x % y = (x & (y - 1))
    }

    @Override
    public boolean containsSpecialRow(SUDA2Item[] items, SUDA2Item referenceItem, int[][] data) {
        // ----------------------------------------------------- //
        startTiming();
        // ----------------------------------------------------- //
        int index = this.offset - 1;
        final int bound = this.array.length;
        for (int offset = 0; offset < bound; offset++) {
            if (array[offset] != 0L) {
                for (int i = 0; i < 64; i++) {
                    if (((array[offset] & (1L << i)) != 0)) {
                        if (containsSpecialRow(items, referenceItem, data[index])) {
                            // ----------------------------------------------------- //
                            endTiming(TYPE_INT_SET_BITS, TYPE_METHOD_SPECIALROW, size);
                            // ----------------------------------------------------- //
                            return true; 
                        }
                    }
                    index ++;
                }
            } else {
                index += 64;
            }
        }
        // ----------------------------------------------------- //
        endTiming(TYPE_INT_SET_BITS, TYPE_METHOD_SPECIALROW, size);
        // ----------------------------------------------------- //
        return false;
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
    public SUDA2IntSet intersectWith(SUDA2IntSet other) {

        // No intersection
        if (this.max < other.min() || other.max() < this.min) {
            return new SUDA2IntSetJump();
        }
        
        // Result is very small
        if (this.size <=8) {
            
            SUDA2IntSetJump result = new SUDA2IntSetJump();
            int index = this.offset;
            final int bound = this.array.length;
            for (int offset = 0; offset < bound; offset++) {
                if (array[offset] != 0L) {
                    for (int i = 0; i < 64; i++) {
                        if (((array[offset] & (1L << i)) != 0)) {
                            result.add(index);
                        }
                        index ++;
                    }
                } else {
                    index += 64;
                }
            }
            return result;

        // Intersect two bitsets
        } else if(other.isBitSet()) {
            
            // ----------------------------------------------------- //
            startTiming();
            // ----------------------------------------------------- //
            
            // Convert and prepare
            SUDA2IntSetBits _other = (SUDA2IntSetBits)other;
            int min = Math.max(this.min, _other.min);
            int max = Math.min(this.max, _other.max);
            
            // Result
            SUDA2IntSetBits result = new SUDA2IntSetBits(min, max);
            result.min = min; // Just an approximation
            result.max = max; // Just an approximation
            
            // Offsets
            int index = offset / 64;
            int _index = _other.offset / 64;
            int resultIndex = result.offset / 64;
            
            // Shift to start at index describing the same offset
            int maxIndex = Math.max(index, Math.max(_index, resultIndex));
            index = maxIndex - index;
            _index = maxIndex - _index;
            resultIndex = maxIndex - resultIndex;
            
            // Pairwise logical and
            while (resultIndex < result.array.length && index < array.length && _index < _other.array.length) {
                long element = array[index++] & _other.array[_index++];
                result.size += Long.bitCount(element);
                result.array[resultIndex++] = element;
            }

            // ----------------------------------------------------- //
            endTiming(TYPE_INT_SET_BITS, TYPE_METHOD_INTERSECTION, size);
            // ----------------------------------------------------- //
            
            return result;
            
        // Let the other set probe this set
        } else {
            
            return other.intersectWith(this);
        }
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

        // Intersect two bitsets
        if (other.isBitSet()) {
            
            // Prepare
            SUDA2IntSetBits _other = (SUDA2IntSetBits)other;

            // Offsets
            int index = offset / 64;
            int _index = _other.offset / 64;
            
            // Shift to start at index describing the same offset
            int maxIndex = Math.max(index, _index);
            index = maxIndex - index;
            _index = maxIndex - _index;
                   
            // And count identical bits
            int count = 0;
            while (count <= 1 && index < array.length && _index < _other.array.length) {
                count += Long.bitCount(array[index++] & _other.array[_index++]);
            }
            
            // ----------------------------------------------------- //
            endTiming(TYPE_INT_SET_BITS, TYPE_METHOD_SUPPORTROW, size);
            // ----------------------------------------------------- //
            
            // Return if we found exactly one such row
            return count == 1;

            // Let the other set probe this set
        } else {
            // ----------------------------------------------------- //
            endTiming(TYPE_INT_SET_BITS, TYPE_METHOD_SUPPORTROW, size);
            // ----------------------------------------------------- //
            return other.isSupportRowPresent(this);
        }
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
        return this.size;
    }

    @Override
    public boolean isBitSet() {
        return true;
    }

    @Override
    public String toString() {
        return "Size=" + size + " offset=" + offset + " array=" + Arrays.toString(array);
    }   
}