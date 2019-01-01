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

import java.util.Set;

/**
 * Listener for MSU discoveries
 * 
 * @author Fabian Prasser
 */
public abstract class SUDA2ListenerKey extends SUDA2Result {

    /**
     * A MSU has been discovered
     * 
     * @param row
     * @param size
     */
    public abstract void keyFound(int row, int size);

    @Override
    void init(int columns, int maxK, int numUniqueRecords, int numDuplicateRecords) {
        // Empty by design
    }

    @Override
    void registerKey(Set<SUDA2Item> set) {
        throw new UnsupportedOperationException("");
    }

    @Override
    void registerKey(SUDA2Item item, SUDA2ItemSet set) {
        SUDA2Item temp = item;
        for (int i = 0; i < set.size(); i++) {
            temp = temp.getProjection(set.get(i).getRows());
        }
        int row = temp.getRows().min();
        int size = set.size() + 1;
        keyFound(row - 1, size);
    }

    @Override
    void registerKey(SUDA2ItemSet set) {
        int row = set.get(0).getRows().min();
        int size = set.size();
        keyFound(row - 1, size);
    }
}
