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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements the SUDA2 algorithm
 * 
 * @author Fabian Prasser
 */
public class SUDA2 {
    
    /** 
     * Little helper class
     * 
     * @author Fabian Prasser
     */
    class Pair<U, V> {

        /** First element*/
        public final U first;
        /** Second element*/
        public final V second;
        
        /**
         * Creates a new instance
         * @param first
         * @param second
         */
        Pair(U first, V second) {
            this.first = first;
            this.second = second;
        }
    }
    
    /** The data */
    private final int[][]         data;
    /** Number of columns */
    private final int             columns;
    /** The result */
    private SUDA2Result           result;
    /** Progress listener */
    private SUDA2ListenerProgress progressListener;
    /** Stop flag */
    private boolean               stop;

    /**
     * Constructor
     * @param data
     */
    public SUDA2(int[][] data) {
        
        // Check and init
        this.check(data);
        this.data = data;
        this.columns = data.length == 0 ? 0 : data[0].length;
    }

    /**
     * Executes the SUDA2 algorithm, calls the callback for each key found
     * 
     * @param maxKeyLength If maxKeyLength <= 0, maxKeyLength will be set to the number of columns
     * @return
     */
    public void getKeys(int maxKeyLength, SUDA2ListenerKey listener) {

        // If maxK <= 0, maxK will be set to the number of columns
        maxKeyLength = maxKeyLength > 0 ? maxKeyLength : columns;

        // Check
        if (isEmpty(data)) {
            return;
        }
        
        // Prepare
        Pair<SUDA2ItemList, Pair<Integer, Integer>> state = getInitialState();
        SUDA2ItemList list = state.first;
        int numUniqueRecords = state.second.first;
        int numDuplicateRecords = state.second.second;
        
        // Execute
        this.result = listener;
        this.result.init(this.columns, maxKeyLength, numUniqueRecords, numDuplicateRecords);
        this.suda2(maxKeyLength, list, data.length);
    }
    
    /**
     * Executes the SUDA2 algorithm, calls the callback for each key found
     * 
     * @return
     */
    public void getKeys(SUDA2ListenerKey listener) {
       getKeys(0, listener); 
    }

    /**
     * Executes the SUDA2 algorithm.
     * 
     * @return
     */
    public SUDA2Statistics getKeyStatistics() {
        return getKeyStatistics(0, false);
    }
    
    /**
     * Executes the SUDA2 algorithm.
     * @param sdcMicroScores Calculate SUDA scores analogously to sdcMicro
     * 
     * @return
     */
    public SUDA2Statistics getKeyStatistics(boolean sdcMicroScores) {
        return getKeyStatistics(0, sdcMicroScores);
    }

    /**
     * Executes the SUDA2 algorithm.
     * 
     * @param maxKeyLength If maxKeyLength <= 0, maxKeyLength will be set to the number of columns
     * @return
     */
    public SUDA2Statistics getKeyStatistics(int maxKeyLength) {
        return getKeyStatistics(maxKeyLength, false);
    }

    /**
     * Executes the SUDA2 algorithm.
     * 
     * @param maxKeyLength If maxKeyLength <= 0, maxKeyLength will be set to the number of columns
     * @param sdcMicroScores Calculate SUDA scores analogously to sdcMicro
     * @return
     */
    public SUDA2Statistics getKeyStatistics(int maxKeyLength, boolean sdcMicroScores) {
        
        // If maxK <= 0, maxK will be set to the number of columns
        maxKeyLength = maxKeyLength > 0 ? maxKeyLength : columns;
        
        // Execute
        this.result = new SUDA2Statistics(this.data.length, this.columns, maxKeyLength, sdcMicroScores);
        
        // Check
        if (isEmpty(this.data)) {
            return (SUDA2Statistics)this.result;
        }
        
        // Prepare
        Pair<SUDA2ItemList, Pair<Integer, Integer>> state = getInitialState();
        SUDA2ItemList list = state.first;
        int numUniqueRecords = state.second.first;
        int numDuplicateRecords = state.second.second;
        
        // Execute
        this.result.init(this.columns, maxKeyLength, numUniqueRecords, numDuplicateRecords);
        this.suda2(maxKeyLength, list, data.length);
        
        // Return
        return (SUDA2Statistics)this.result;
    }
    
    /**
     * Sets a progress listener
     * @param progressListener
     */
    public void setProgressListener(SUDA2ListenerProgress progressListener) {
        this.progressListener = progressListener;
    }

    /**
     * Stops the process
     * @param stop
     */
    public void stop() {
        this.stop = true;
    }
        
    /**
     * Check argument
     * @param data
     */
    private void check(int[][] data) {
        if (data == null || (data.length > 0 && data[0] == null)) {
            throw new NullPointerException("Data must not be null");
        }
    }
    
    /**
     * Returns the initial state needed for executing the algorithm
     * @return
     */
    private Pair<SUDA2ItemList, Pair<Integer, Integer>> getInitialState() {

        // Collect all items and their support rows
        SUDA2IndexedItemSet items = new SUDA2IndexedItemSet();
        SUDA2Groupify groupify = new SUDA2Groupify(data.length);
        int index = 1; // Value 0 is reserved for empty entries in SUDA2IntSet
        for (int[] row : data) {
            if (!groupify.canBeIgnored(row)) {
                for (int column = 0; column < columns; column++) {
                    int value = row[column];
                    SUDA2Item item = items.getOrCreateItem(column, value);
                    item.addRow(index);
                }
            }
            index++;
        }
        return new Pair<>(items.getItemList(), new Pair<>(groupify.getNumUniqueRecords(), groupify.getNumDuplicateRecords()));
    }

    /**
     * Returns all items for the given reference item from the given list, starting at fromIndex (included)
     * This means that all 1-MSUs can be removed beforehand.
     * @param itemList
     * @param reference
     * @param fromIndex 
     * @return
     */
    private SUDA2IndexedItemSet getItems(SUDA2ItemList itemList, SUDA2Item reference, int fromIndex) {

        // For all items within the given range
        SUDA2IndexedItemSet items = new SUDA2IndexedItemSet();
        List<SUDA2Item> list = itemList.getList();
        SUDA2IntSet referenceRows = reference.getRows();
        for (int index = fromIndex; index < list.size(); index++) {
            
            // Extract item of interest
            SUDA2Item item = list.get(index).getProjection(referenceRows);
                        
            // If it is contained, add it
            if (item != null) {
                items.addItem(item);
            }
        }
        
        // Return all items
        return items;
    }

    /**
     * Clears the list and returns all MSUs
     * @param list
     * @param numRecords
     * @return
     */
    private Pair<List<SUDA2ItemSet>, SUDA2ItemList> getMSUs(SUDA2ItemList list, int numRecords) {
        
        // Prepare
        List<SUDA2ItemSet> msus = new ArrayList<>();
        
        // Check the items
        List<SUDA2Item> result = new ArrayList<SUDA2Item>();
        for (SUDA2Item item : list.getList()) {

            // All unique items are already MSUs
            if (item.getSupport() == 1) {
                msus.add(new SUDA2ItemSet(item));

            // All items appearing in all rows can be ignored
            } else if (item.getSupport() != numRecords) {
                result.add(item);
            }
        }

        // Return
        return new Pair<List<SUDA2ItemSet>, SUDA2ItemList>(msus, new SUDA2ItemList(result));
    }

    /**
     * Returns all 1-MSUS for the given reference item from the given list, starting at fromIndex (included)
     * 
     * @param itemList
     * @param reference
     * @param fromIndex 
     * @return
     */
    private List<SUDA2ItemSet> getMSUs(SUDA2ItemList itemList, SUDA2Item reference, int fromIndex) {

        // For all items within the given range
        List<SUDA2ItemSet> result = new ArrayList<>();
        List<SUDA2Item> list = itemList.getList();
        SUDA2IntSet referenceRows = reference.getRows();
        for (int index = fromIndex; index < list.size(); index++) {
            SUDA2Item item = list.get(index).get1MSU(referenceRows);
            if (item != null) {
                // TODO: Get rid of itemset
                result.add(new SUDA2ItemSet(item));
            }
        }
        return result;
    }

    /**
     * Check data
     * @param data
     * @return
     */
    private boolean isEmpty(int[][] data) {
        return data.length == 0 || data[0].length == 0;
    }

    /**
     * Implements both checks for MSUs described in the paper
     * @param currentList
     * @param candidate
     * @param referenceItem
     * @return
     */

    private boolean isMSU(final SUDA2ItemList currentList,
                          SUDA2ItemSet candidate,
                          SUDA2Item referenceItem) {

        // All of the k-1 items in the candidate set must have rank > reference rank
        // We don't need to check this, because we have only used items with higher
        // ranks when performing the recursive call, anyways.
        
        // We don't need to search for the special row for candidate item sets of size 1
        if (candidate.size() <= 1) {
            return true;
        }
         
        // Search for the special row
        // This is one of the hottest functions in SUDA2
        // (1) It seems to be more likely that the special row is found than that
        //     the special row is not found -> We optimize for this path
        // (2) It is more likely that not all of the candidate items are contained
        //     than that the reference item is contained
        
        // Find item with smallest support 
        SUDA2IntSet rows = null;
        SUDA2Item pivot = null;
        int candidateSize = candidate.size();
        for (int i = 0; i < candidateSize; i++) {
            SUDA2Item item = candidate.get(i);
            SUDA2IntSet _rows = currentList.getItem(item.getId()).getRows();
            if (rows == null || _rows.size < rows.size) {
                rows = _rows;
                pivot = item;
            }
        }
        
        // Prepare list of items to check
        SUDA2Item[] items = new SUDA2Item[candidate.size()-1];
        int index = 0;
        for (int i = 0; i < candidateSize; i++) {
            SUDA2Item item = candidate.get(i);
            if (item != pivot) {
                items[index++] = item;
            }
        }
        Arrays.sort(items, new Comparator<SUDA2Item>() {
            @Override
            public int compare(SUDA2Item o1, SUDA2Item o2) {
                int support1 = currentList.getItem(o1.getId()).getSupport();
                int support2 = currentList.getItem(o1.getId()).getSupport();
                return support1 < support2 ? -1 :
                       support1 > support2 ? +1 : 0;
            }
        });
        
        // And search for the special row
        final int [] buckets = rows.buckets;
        outer: for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != 0) {
                int[] row = data[buckets[i] - 1];
                for (SUDA2Item item : items) {
                    if (!item.isContained(row)) {
                        continue outer;
                    }
                }
                if (referenceItem.isContained(row)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * SUDA2
     * @param maxK
     * @param currentList
     * @param numRecords
     * @return
     */
    private List<SUDA2ItemSet> suda2(int maxK,
                                    SUDA2ItemList currentList,
                                    int numRecords) {

        // Find MSUs and clear list
        Pair<List<SUDA2ItemSet>, SUDA2ItemList> msusAndList = getMSUs(currentList, numRecords);
        List<SUDA2ItemSet> msus = msusAndList.first;
        currentList = msusAndList.second;
        
        // When processing the original table
        if (numRecords == data.length) {
            
            // Register 1-MSUs for the original table
            for (SUDA2ItemSet msu : msus) {
                result.registerKey(msu);
            }
        } 
        
        if (stop) {
            throw new SUDA2Exception("Interrupted");
        }

        // Check for maxK
        if (maxK <= 1) {
            return msus;
        }

        // For each item i
        int index = 0;
        int total = currentList.getList().size();
        for (SUDA2Item referenceItem : currentList.getList()) {
            
            // Track
            index++;
            
            // Progress information
            if (numRecords == data.length && progressListener != null) {
             
                progressListener.update((double)index / (double)total);
            }

            // Recursive call
            int upperLimit = maxK - 1; // Pruning strategy 3
            upperLimit = Math.min(upperLimit, currentList.size() - index); // Pruning strategy 2
            upperLimit = Math.min(upperLimit, referenceItem.getSupport() - 1); // Pruning strategy 1
            
            // We only perform recursion for maxK > 1
            List<SUDA2ItemSet> msus_i;
            if (upperLimit > 1) {
                msus_i = suda2(upperLimit,
                               getItems(currentList, referenceItem, index).getItemList(),
                               referenceItem.getRows().size);
            } else {
                msus_i = getMSUs(currentList, referenceItem, index);
            }

            // For each candidate
            outer: for (SUDA2ItemSet candidate : msus_i) {
                
                // Check if candidate is an MSU
                if (!isMSU(currentList, candidate, referenceItem)) {
                    continue outer;
                }

                // Add MSU
                if (numRecords == data.length) {
                    result.registerKey(referenceItem, candidate);
                } else {
                    candidate.add(referenceItem);
                    msus.add(candidate);
                }
            }
        }
        
        // Return
        return msus;
    }
}
