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
 * A class for calculating SUDA scores
 * 
 * @author Fabian Prasser
 * @author Raffael Bild
 */
public class SUDA2StatisticsScores extends SUDA2ListenerScore {
    
    /**
     * Returns SUDA DIS scores for the given scores calculated as described in
     * Mark Elliot, Special Uniques Detection Algorithm - User Guide, September 2004
     * @param scores
     * @param numUniqueRecords Number of records in equivalence classes of size 1
     * @param numDuplicateRecords Number of records in equivalence classes of size 2
     * @param columns
     * @param disFraction Default: 0.01d
     * @return
     */
    public static double[] getScoreDIS(double[] scores, long numUniqueRecords, long numDuplicateRecords, int columns, double disFraction) {
        
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
     * Returns an array containing the scores for msus of different sizes as originally described by Elliot et al.
     * @param columns
     * @param maxK
     * @return
     */
    public static double[] getScoresElliot(int columns, int maxK) {     
        
        double[] scores = new double[maxK];
        for (int size = 1; size <= maxK; size++) {
            double score = getScoreElliot(columns, maxK, size);
            scores[size - 1] = score;
        }
        return scores;
    }
    /**
     * Returns an array containing the scores for msus of different sizes as implemented by sdcMicro
     * @param columns
     * @param maxK
     * @return
     */
    public static double[] getScoresSDCMicro(int columns, int maxK) {     
        
        double[] scores = new double[maxK];
        for (int size = 1; size <= maxK; size++) {
            double score = getScoreSdcMicro(columns, maxK, size);
            scores[size - 1] = score;
        }
        return scores;
    }
    /**
     * Calculates the score for an MSU as originally described by Elliot et al.
     * @param columns
     * @param maxK
     * @param size
     * @return
     */
    private static double getScoreElliot(int columns, int maxK, int size) {
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
    private static double getScoreSdcMicro(int columns, int maxK, int size) {
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

    /** Per record scores */
    private double[] recordScores;
    /** The number of MSUs */
    private long     numKeys    = 0;
    /** Risk distribution */
    private double   totalScore = 0d;
    /** Risk distribution */
    private double   highestScore = 0d;
    /** Columns */
    private int      columns;
    /** Records in classes of size 1 */
    private int      numUniqueRecords = 0;
    /** Records in classes of size 2 */
    private int      numDuplicateRecords = 0;
    /** Maximal size of an MSU considered */
    private final int      maxK;
    
    /**
     * Creates a new instance
     * @param rows
     * @param columns
     * @param maxK
     * @param sdcMicroScores
     */
    SUDA2StatisticsScores(int rows, int columns, int maxK, boolean sdcMicroScores) {
        super(sdcMicroScores);
        this.recordScores = new double[rows];
        this.columns = columns;
        this.maxK = maxK;
    }

    /**
     * Returns the average score
     * @return
     */
    public double getAverageScore() {
        return (double)this.totalScore / (double)this.numKeys;
    }

    /**
     * Returns the maximal size which has been searched for
     * @return
     */
    public int getMaxKeyLengthConsidered() {
        return this.maxK;
    }
    
    /**
     * Returns the highest score
     * @return
     */
    public double getHighestScore() {
        return this.highestScore;
    }
    
    /**
     * Returns the DIS scores
     * @param samplingFraction Proportion of the population
     * @return
     */
    public double[] getDISScores(double samplingFraction) {
        return SUDA2StatisticsScores.getScoreDIS(recordScores, numUniqueRecords, numDuplicateRecords, columns, samplingFraction);
    }
    
    /**
     * Returns the number of MSUs found
     * @return
     */
    public long getNumKeys() {
        return this.numKeys;
    }
    
    /**
     * Returns the scores for all records
     * @return
     */
    public double[] getSUDAScores() {
        return this.recordScores;
    }
    
    @Override
    public void scoreFound(int row, int size, double score) {
        this.recordScores[row] = score;
        this.totalScore += score;
        this.highestScore = Math.max(this.highestScore, score);
        this.numKeys++;
    }
    
    @Override
    void init(int columns, int maxK, int numUniqueRecords, int numDuplicateRecords) {
        super.init(columns, maxK, numUniqueRecords, numDuplicateRecords);
        this.numUniqueRecords = numUniqueRecords;
        this.numDuplicateRecords = numDuplicateRecords;
    }
}
