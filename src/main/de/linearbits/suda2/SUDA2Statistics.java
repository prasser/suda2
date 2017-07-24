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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Set;

/**
 * The result of executing SUDA2
 * 
 * @author Fabian Prasser
 */
public class SUDA2Statistics extends SUDA2Result {

    /** Intermediate scores */
    private final double[] intermediateScores;
    /** Num. columns */
    private final int      columns;
    /** Maximal size of an MSU considered */
    private final int      maxK;
    /** The number of MSUs */
    private long           numKeys      = 0;
    /** The total size of MSUs */
    private long           totalKeySize = 0;
    /** Contributions of each column */
    private final double[] columnKeyContributions;
    /** Contributions of each column */
    private final double[] columnKeyTotals;
    /** Contributions of each column */
    private final double[] columnKeyCounts;
    /** Distribution of sizes of MSUs */
    private final double[] sizeDistribution;
    /** Risk distribution */
    private double         totalScore   = 0d;

    /**
     * Creates a new instance
     * @param columns
     * @param sdcMicroScores
     * @param maxK
     */
    SUDA2Statistics(int rows, int columns, int maxK, boolean sdcMicroScores) {
        
        // Init
        this.columns = columns;
        this.maxK = maxK;
        this.columnKeyContributions = new double[columns];
        this.columnKeyTotals = new double[columns];
        this.columnKeyCounts = new double[columns];
        this.sizeDistribution = new double[maxK];
        this.intermediateScores = new SUDA2Scores(columns, maxK, sdcMicroScores).getIntermediateScores();

    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SUDA2Statistics other = (SUDA2Statistics) obj;
        if (!Arrays.equals(columnKeyContributions, other.columnKeyContributions)) return false;
        if (!Arrays.equals(columnKeyCounts, other.columnKeyCounts)) return false;
        if (!Arrays.equals(columnKeyTotals, other.columnKeyTotals)) return false;
        if (maxK != other.maxK) return false;
        if (numKeys != other.numKeys) return false;
        if (!Arrays.equals(sizeDistribution, other.sizeDistribution)) return false;
        if (totalKeySize != other.totalKeySize) return false;
        if (Double.doubleToLongBits(totalScore) != Double.doubleToLongBits(other.totalScore)) return false;
        return true;
    }
    
    /**
     * Returns the average key size
     * @return
     */
    public double getAverageKeySize() {
        return (double)this.totalKeySize / (double)this.numKeys;
    }
    
    /**
     * Returns the average key size per column
     * @return
     */
    public double[] getColumnAverageKeySize() {
        double[] result = new double[this.columnKeyTotals.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (double)this.columnKeyTotals[i] / (double)this.columnKeyCounts[i];
        }
        return result;
    }

    /**
     * Returns the contributions of each column to the total score
     * @return
     */
    public double[] getColumnKeyContributions() {
        double[] result = new double[this.columnKeyContributions.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (double)this.columnKeyContributions[i] / (double)this.totalScore;
        }
        return result;
    }

    /**
     * Returns the distribution of the sizes of MSUs
     * @return
     */
    public double[] getKeySizeDistribution() {
        double[] result = new double[this.sizeDistribution.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (double)this.sizeDistribution[i] / (double)this.numKeys;
        }
        return result;
    }

    /**
     * Returns the maximal size which has been searched for
     * @return
     */
    public int getMaxKeyLength() {
        return this.maxK;
    }
    
    /**
     * Returns the number of columns considered
     * @return
     */
    public int getNumColumns() {
        return this.columns;
    }
    
    /**
     * Returns the number of MSUs found
     * @return
     */
    public long getNumKeys() {
        return this.numKeys;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(columnKeyContributions);
        result = prime * result + Arrays.hashCode(columnKeyCounts);
        result = prime * result + Arrays.hashCode(columnKeyTotals);
        result = prime * result + maxK;
        result = prime * result + (int) (numKeys ^ (numKeys >>> 32));
        result = prime * result + Arrays.hashCode(sizeDistribution);
        result = prime * result + (int) (totalKeySize ^ (totalKeySize >>> 32));
        long temp;
        temp = Double.doubleToLongBits(totalScore);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    
    @Override
    public String toString() {
        
        // Prepare
        double[] totalsContributions = new double[columns];
        Arrays.fill(totalsContributions, totalScore);        
        double[] totalsSize = new double[maxK];
        Arrays.fill(totalsSize, numKeys);
        
        // Render
        StringBuilder builder = new StringBuilder();
        builder.append("Minimal Sample Uniques\n");
        builder.append(" - Number of columns: ").append(this.columns).append("\n");
        builder.append(" - Number of keys: ").append(this.numKeys).append("\n");
        builder.append(" - Average size of keys: ").append(this.getAverageKeySize()).append("\n");
        builder.append(" - Column key contributions\n");
        builder.append(toString("     ", columnKeyContributions, totalsContributions));
        builder.append(" - Column key average size\n");
        builder.append(toString("     ", columnKeyTotals, columnKeyCounts));
        builder.append(" - Key size distribution\n");
        builder.append(toString("     ", sizeDistribution, totalsSize));
        return builder.toString();
    }
    
    /**
     * Renders a distribution
     * @param intent
     * @param array
     * @param offset
     * @param absolute
     * @return
     */
    private String toString(String intent, double[] array, double[] totals) {
        
        StringBuilder builder = new StringBuilder();
        DecimalFormat integerFormat = new DecimalFormat("#######");
        DecimalFormat doubleFormat = new DecimalFormat("###.###");
        final int VALUE_WIDTH = 7;
        builder.append(intent).append("|");
        for (int index = 0; index < array.length; index++) {
            builder.append(toString(integerFormat.format(index), VALUE_WIDTH)).append("|");
        }
        int width = builder.length() - intent.length();
        builder.append("\n");
        builder.append(intent);
        for (int i = 0; i < width; i++) {
            builder.append("-");
        }
        builder.append("\n");
        builder.append(intent).append("|");
        for (int i = 0; i < array.length; i++) {
            double _value = array[i];
            double _total = totals[i];
            double _result = _total != 0d ? _value / _total : 0d;
            String value = doubleFormat.format(_result).replace(',', '.');
            if (value.equals("0") && _value > 0) value = "~0";
            builder.append(toString(value, VALUE_WIDTH)).append("|");
        }
        builder.append("\n");
        return builder.toString();
    }

    /**
     * Makes sure that the value has the given number of characters
     * @param value
     * @param width
     * @return
     */
    private String toString(String value, int width) {
        while (value.length() < width) {
            value = " " + value;
        }
        return value;
    }
    
    @Override
    void init(int columns, int maxK, int numUniqueRecords, int numDuplicateRecords) {
        // Empty by design
    }

    @Override
    void registerKey(Set<SUDA2Item> set) {
        this.numKeys++;
        this.totalKeySize += set.size();
        this.sizeDistribution[set.size() - 1]++;
        double score = intermediateScores[set.size() - 1];
        this.totalScore += score;
        for (SUDA2Item item : set) {
            int column = item.getColumn();
            this.columnKeyContributions[column] += score;
            this.columnKeyTotals[column] += set.size();
            this.columnKeyCounts[column]++;
        }
    }

    @Override
    void registerKey(SUDA2Item item, SUDA2ItemSet set) {
        this.numKeys++;
        this.totalKeySize += set.size() + 1;
        this.sizeDistribution[set.size()]++;
        int size = set.size();
        double score = intermediateScores[size];
        this.totalScore += score;
        for (int i = 0; i < size; i++) {
            int column = set.get(i).getColumn();
            this.columnKeyContributions[column] += score;
            this.columnKeyTotals[column] += set.size() + 1;
            this.columnKeyCounts[column]++;
        }
        this.columnKeyContributions[item.getColumn()] += score;
        this.columnKeyTotals[item.getColumn()] += set.size() + 1;
        this.columnKeyCounts[item.getColumn()]++;
    }

    @Override
    void registerKey(SUDA2ItemSet set) {
        this.numKeys++;
        this.totalKeySize += set.size();
        this.sizeDistribution[set.size() - 1]++;
        int size = set.size();
        double score = intermediateScores[size - 1];
        this.totalScore += score;
        for (int i = 0; i < size; i++) {
            int column = set.get(i).getColumn();
            this.columnKeyContributions[column] += score;
            this.columnKeyTotals[column] += set.size();
            this.columnKeyCounts[column]++;
        }
    }
}
