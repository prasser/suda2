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

import java.util.Arrays;

/**
 * A class for calculating SUDA scores
 * 
 * @author Fabian Prasser
 * @author Raffael Bild
 */
class SUDA2Scores {
    
    /** Scores for testing */
    private static final double[] SCORES = { 7d, 0d, 0d, 1.75d, 0d, 3.25d, 0d, 1.75d, 2.75d, 0d, 0d };
    
    /** Expected results for testing */
    private static final double[] DIS    = { 0.028038903d, 0d, 0d,
                                             0.005435918d, 0d, 0.011357785d,
                                             0d, 0.005435918d, 0.009313862d, 0d, 0d};

    /**
     * Main method for testing
     * @param args
     */
    public static void main(String[] args) {
        long numUniqueRecords = 5; // 5
        long numDuplicateRecords = 6; // 6
        int columns = 4;
        double disFraction = 0.01d; // 0.01d;
        SUDA2Scores calc = new SUDA2Scores(4, 4, true);
        System.out.println(Arrays.toString(calc.getScoreDIS(SCORES, numUniqueRecords, numDuplicateRecords, columns, disFraction)));
        System.out.println(Arrays.toString(DIS));
    }
    
    /** Intermediate scores */
    private final double[] scores;

    /**
     * Creates a new instance
     * @param columns
     * @param maximum key size
     * @param sdcMicro
     */
    SUDA2Scores(int columns, int maxK, boolean sdcMicro) {
        this.scores = new double[maxK];
        for (int size = 1; size <= maxK; size++) {
            double score = sdcMicro ? getScoreSdcMicro(columns, maxK, size) : 
                                      getScoreElliot(columns, maxK, size);
            this.scores[size - 1] = score;
        }
    }

    /**
     * Returns SUDA DIS scores for the given scores calculated as described in
     * "Special Uniques Detection Algorithm - User Guide"
     * (https://www.click2go.umip.com/i/s_w/suda.html)
     * @param scores
     * @param numUniqueRecords Number of records in equivalence classes of size 1
     * @param numDuplicateRecords Number of records in equivalence classes of size 2
     * @param columns
     * @param disFraction Default: 0.01d
     * @return
     */
    private double[] getScoreDIS(double[] scores, 
                                 long numUniqueRecords, long numDuplicateRecords, 
                                 int columns, double disFraction) {
        
        // Prepare
        double[] result = new double[scores.length];
        double parameterQ = 1.0d + (8.0d - (double) columns) / 20.0d;
        disFraction = (double)numUniqueRecords * disFraction / 
                     ((double)numUniqueRecords * disFraction + (double)numDuplicateRecords * (1.0d - disFraction));

        // Adjustment factor
        double adjustmentFactor = 0.0;
        for (double score : scores) {
            adjustmentFactor += (score > 0) ? (1.0d / Math.pow(score, parameterQ)) : 0d;
        }
        adjustmentFactor = ((double)numUniqueRecords / disFraction - (double)numUniqueRecords) / adjustmentFactor;
        
        // Calculate DIS scores
        for (int i=0; i< scores.length; i++) {
            result[i] = (scores[i] > 0) ? (1.0d / (1.0d + Math.pow(scores[i], -parameterQ) * adjustmentFactor)) : 0d;
        }
        
        // Return
        return result;
    }
    
    /**
     * Calculates the score for an MSU as originally described by Elliot et al.
     * @param columns
     * @param maxK
     * @param size
     * @return
     */
    private double getScoreElliot(int columns, int maxK, int size) {
        final int UPPER = Math.min(maxK, columns - 1);
        double score = 1d;
        for (int i = size; i < UPPER; i++) {
            score *= (double) (columns - i);
        }
        return score;
    }
    
    /**
     * Calculates the score for an MSU as implemented by sdcMicro
     * @param columns
     * @param maxK
     * @param size
     * @return
     */
    private double getScoreSdcMicro(int columns, int maxK, int size) {
        final int UPPER = columns-size;
        double score = Math.pow(2d, UPPER) - 1d;
        for (int j = 2; j <= size; j++) {
            score *= j;
        }
        for (int k = 2; k <= UPPER; k++) {
            score *= k;
        }
        double factorial = 1d;
        for (int i = 2; i <= columns; i++) {
            factorial *= (double)i;
        }
        return score / factorial;
    }
    
    /**
     * Returns the intermediate scores
     * @return
     */
    double[] getIntermediateScores() {
        return this.scores;
    }
}
