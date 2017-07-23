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
class SUDA2Scores {

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
