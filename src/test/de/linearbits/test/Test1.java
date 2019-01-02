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

import de.linearbits.suda2.SUDA2;
import de.linearbits.suda2.SUDA2StatisticsKeys;

/**
 * Test class
 * 
 * @author Fabian Prasser
 */
public class Test1 extends AbstractTest {

    /**
     * Main entry point
     * @param args
     */
    public static void main(String[] args) {

        // A simple example dataset
        int[][] data = new int[][] {
                new int[] { 0, 3, 0, 1, 1 }, // 0 
                new int[] { 0, 3, 0, 0, 1 }, // 1 
                new int[] { 0, 3, 1, 1, 1 }, // 2
                new int[] { 1, 3, 0, 1, 2 }, // 3 
                new int[] { 0, 2, 0, 1, 2 }, // 4 
                new int[] { 1, 2, 1, 0, 2 }  // 5
        };

        SUDA2StatisticsKeys result = new SUDA2(data).getStatisticsKeys(0);
        System.out.println(result.toString());
    }
}
