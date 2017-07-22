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
 * Test based on the example from the sdcMicro handbook
 * 
 * @author Fabian Prasser
 */
public class Test3 extends AbstractTest{

    /**
     * Main entry point
     * @param args
     */
    public static void main(String[] args) {
        
        // From sdcMicro handbook
        int[][] data = new int[][] {
                new int[] {1, 2, 2, 1},
                new int[] {1, 2, 1, 1},
                new int[] {1, 2, 1, 1},
                new int[] {3, 3, 1, 5},
                new int[] {4, 3, 1, 4},
                new int[] {4, 3, 1, 1},
                new int[] {6, 2, 1, 5},
                new int[] {1, 2, 2, 1},
        };
        print(data);
        System.out.println("\n-----\n"); 
        SUDA2Result result1 = new SUDA2(data).getMSUStatistics(5);
        System.out.println(result1.toString());
        System.out.println("\n-----\n");
        SUDA2Result result2 = new ExhaustiveSearch(data).getMSUStatistics();
        System.out.println(result2.toString());
    }
}
