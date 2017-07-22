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

import de.linearbits.suda2.ExhaustiveSearch;
import de.linearbits.suda2.SUDA2;
import de.linearbits.suda2.SUDA2Result;

/**
 * Test class
 * 
 * @author Fabian Prasser
 */
public class Test2 extends AbstractTest {

    /**
     * Main entry point
     * @param args
     */
    public static void main(String[] args) {
        
        // Simple data
        int[][] data = new int[][] {
                new int[] {1, 2, 3},
                new int[] {2, 2, 3},
                new int[] {1, 1, 3}
        };
        print(data);
        System.out.println("\n-----\n"); 
        SUDA2Result result1 = new SUDA2(data).getMSUStatistics(0);
        System.out.println(result1.toString());
        System.out.println("\n-----\n");
        SUDA2Result result2 = new ExhaustiveSearch(data).getMSUStatistics();
        System.out.println(result2.toString());
        
        /* sdcMicro
         * Contributions Var-1: 57.14286
         * Contributions Var-2: 57.14286
         * Contributions Var-3: 0
         */
    }
}
