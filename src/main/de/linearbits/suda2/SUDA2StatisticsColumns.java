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
public class SUDA2StatisticsColumns extends SUDA2Result {

    /** Intermediate scores */
    private final double[] intermediateScores;
    /** MaxK */
    private final int      maxK;
    /** The total size of MSUs */
    private long           totalKeySize = 0;
    /** Columns */
    private final int      columns;
    /** Contributions of each column */
    private final double[] columnKeyContributions;
    /** Contributions of each column */
    private final double[] columnKeyTotals;
    /** Contributions of each column */
    private final double[] columnKeyCounts;
    /** Risk distribution */
    private double         totalScore   = 0d;

    /**
     * Creates a new instance
     * @param columns
     * @param sdcMicroScores
     * @param maxK
     */
    SUDA2StatisticsColumns(int rows, int columns, int maxK, boolean sdcMicroScores) {
        
        // Init
        this.columns = columns;
        this.maxK = maxK;
        this.columnKeyContributions = new double[columns];
        this.columnKeyTotals = new double[columns];
        this.columnKeyCounts = new double[columns];
        if (sdcMicroScores) {
            this.intermediateScores = SUDA2StatisticsScores.getScoresSDCMicro(columns, maxK);
        } else {
            this.intermediateScores = SUDA2StatisticsScores.getScoresElliot(columns, maxK);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SUDA2StatisticsColumns other = (SUDA2StatisticsColumns) obj;
        if (!Arrays.equals(columnKeyContributions, other.columnKeyContributions)) return false;
        if (!Arrays.equals(columnKeyCounts, other.columnKeyCounts)) return false;
        if (!Arrays.equals(columnKeyTotals, other.columnKeyTotals)) return false;
        if (totalKeySize != other.totalKeySize) return false;
        if (Double.doubleToLongBits(totalScore) != Double.doubleToLongBits(other.totalScore)) return false;
        return true;
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
     * Returns the maximal size which has been searched for
     * @return
     */
    public int getMaxKeyLengthConsidered() {
        return this.maxK;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(columnKeyContributions);
        result = prime * result + Arrays.hashCode(columnKeyCounts);
        result = prime * result + Arrays.hashCode(columnKeyTotals);
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
    
        // Render
        StringBuilder builder = new StringBuilder();
        builder.append("Minimal Sample Uniques\n");
        builder.append(" - Number of columns: ").append(this.columns).append("\n");
        builder.append(" - Column key contributions\n");
        builder.append(toString("     ", columnKeyContributions, totalsContributions, 0));
        builder.append(" - Column key average size\n");
        builder.append(toString("     ", columnKeyTotals, columnKeyCounts, 0));
        return builder.toString();
    }
    
    /**
     * Renders a distribution
     * @param intent
     * @param array
     * @param totals
     * @param offset
     * @return
     */
    private String toString(String intent, double[] array, double[] totals, int offset) {
        
        StringBuilder builder = new StringBuilder();
        DecimalFormat integerFormat = new DecimalFormat("#######");
        DecimalFormat doubleFormat = new DecimalFormat("###.###");
        final int VALUE_WIDTH = 7;
        builder.append(intent).append("|");
        for (int index = 0; index < array.length; index++) {
            builder.append(toString(integerFormat.format(index + offset), VALUE_WIDTH)).append("|");
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
        this.totalKeySize += set.size();
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
        this.totalKeySize += set.size() + 1;
        double score = intermediateScores[set.size()];
        this.totalScore += score;
        for (int i = 0; i < set.size(); i++) {
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
        this.totalKeySize += set.size();
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
