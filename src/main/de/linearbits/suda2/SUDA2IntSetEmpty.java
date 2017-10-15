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
public class SUDA2IntSetEmpty extends SUDA2IntSet {

    @Override
    public void add(int value) {
        throw new IllegalStateException("No data must be added to an empty set");
    }

    @Override
    public boolean contains(int value) {
        return false;
    }
    
    @Override
    public boolean containsSpecialRow(SUDA2Item[] items, SUDA2Item referenceItem, int[][] data) {
        return false;
    }

    @Override
    public Type getType() {
        return Type.EMPTY;
    }

    @Override
    public SUDA2IntSet intersectWith(SUDA2IntSet other) {
        return this;
    }

    @Override
    public boolean isSupportRowPresent(SUDA2IntSet other) {
        return false;
    }

    @Override
    public int max() {
        return 0;
    }

    @Override
    public int min() {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String toString() {
        return "EmptySet";
    }
}
