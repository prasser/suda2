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
package de.linearbits.test;

import java.util.Arrays;

import de.linearbits.suda2.SUDA2StatisticsScores;

/**
 * Test for DIS scores
 * 
 * @author Fabian Prasser
 * @author Raffael Bild
 */
public class Test10 extends AbstractTest {

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
        System.out.println(Arrays.toString(SUDA2StatisticsScores.getScoreDIS(SCORES, numUniqueRecords, numDuplicateRecords, columns, disFraction)));
        System.out.println(Arrays.toString(DIS));
    }
    
}
