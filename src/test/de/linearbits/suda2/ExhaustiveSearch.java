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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple exhaustive search for tests
 * 
 * @author Fabian Prasser
 */
public class ExhaustiveSearch extends SUDA2Statistics{

    /**
     * Generates the power set
     * @param set
     * @return
     */
    public static <T> Set<Set<T>> powerSet(Set<T> set) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (set.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(set);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> _set : powerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(_set);
            sets.add(newSet);
            sets.add(_set);
        }
        return sets;
    }

    /** The data */
    private final int[][] data;
    /** The number of columns*/
    private final int     columns;

    /**
     * Creates a new instance
     * 
     * @param data
     */
    public ExhaustiveSearch(int[][] data) {
        super(data.length, data[0].length, 2, true);
        this.data = data;
        this.columns = data[0].length;
    }

    /**
     * Performs an exhaustive search for comparison
     * @return
     */
    public SUDA2Result getMSUStatistics() {

        Map<Set<SUDA2Item>, Integer> counts = new HashMap<Set<SUDA2Item>, Integer>();

        // Collect the power set of all items in each row and count how often they occur
        for (int[] row : data) {
            // Set of items for this row
            Set<SUDA2Item> items = new HashSet<SUDA2Item>();
            for (int column = 0; column < columns; column++) {
                int value = row[column];
                items.add(new SUDA2Item(column, value, SUDA2Item.getId(column, value)));
            }

            // Extract power set
            for (Set<SUDA2Item> set : powerSet(items)) {

                if (!set.isEmpty()) {

                    // Count number of occurrences
                    if (!counts.containsKey(set)) {
                        counts.put(set, 1);
                    } else {
                        counts.put(set, counts.get(set) + 1);
                    }
                }
            }
        }

        // Create a set and a list containing the items
        Set<Set<SUDA2Item>> result = new HashSet<>();
        result.addAll(counts.keySet());

        // Extract all item sets that occur only once
        Iterator<Set<SUDA2Item>> iter = result.iterator();
        while (iter.hasNext()) {
            if (counts.get(iter.next()) > 1) {
                iter.remove();
            }
        }

        // Now remove all item sets which contain any of the other item sets
        List<Set<SUDA2Item>> list = new ArrayList<>();
        list.addAll(result);
        int size = list.size();
        int previous = 0;
        while (size != previous) {

            // For each itemset, pivot, in this list
            for (Set<SUDA2Item> pivot : list) {

                // Remove all itemsets from the set that contain all elements
                // from pivot
                iter = result.iterator();
                while (iter.hasNext()) {
                    Set<SUDA2Item> current = iter.next();
                    if (current != pivot && current.containsAll(pivot)) {
                        iter.remove();
                    }
                }
            }
            previous = size;
            size = result.size();
            list.clear();
            list.addAll(result);
        }

        SUDA2Result _result = new SUDA2Statistics(this.data.length, this.columns, this.columns, false);
        for (Set<SUDA2Item> msu : result) {
            _result.registerMSU(msu);
        }
        return _result;
    }
}
