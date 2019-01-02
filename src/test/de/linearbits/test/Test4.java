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
import de.linearbits.suda2.SUDA2Result;

/**
 * Test based on the example from: 
 * IHSN - STATISTICAL DISCLOSURE CONTROL FOR MICRODATA: A PRACTICE GUIDE
 * 
 * @author Fabian Prasser
 * @author Raffael Bild
 */
public class Test4 extends AbstractTest{

    /**
     * Main entry point
     * @param args
     */
    public static void main(String[] args) {
        
        // Data from: IHSN - STATISTICAL DISCLOSURE CONTROL FOR MICRODATA: A PRACTICE GUIDE
        /*1 Urban, Female, Secondary incomplete, Employed*/
        /*2 Urban, Female, Secondary incomplete, Employed*/
        /*3 Urban, Female, Primary incomplete, Non-LF*/
        /*4 Urban, Male, Secondary complete, Employed*/
        /*5 Rural, Female, Secondary complete, Unemployed*/
        /*6 Urban, Male, Secondary complete, Employed*/
        /*7 Urban, Female, Primary complete, Non-LF*/
        /*8 Urban, Male, Post-secondary, Unemployed*/
        /*9 Urban, Female, Secondary incomplete, Non-LF*/
        /*10 Urban, Female, Secondary incomplete, Non-LF*/
        
        // As array
        int[][] data = new int[][] {
               /*1*/ new int[] {0, 0, 0, 0},
               /*2*/ new int[] {0, 0, 0, 0},
               /*3*/ new int[] {0, 0, 1, 1},
               /*4*/ new int[] {0, 1, 2, 0},
               /*5*/ new int[] {1, 0, 2, 2},
               /*6*/ new int[] {0, 1, 2, 0},
               /*7*/ new int[] {0, 0, 3, 1},
               /*8*/ new int[] {0, 1, 4, 2},
               /*9*/ new int[] {0, 0, 0, 1},
              /*10*/ new int[] {0, 0, 0, 1}
        };
        print(data);
        System.out.println("\n-----\n"); 
        SUDA2Result result1 = new SUDA2(data).getStatisticsKeys(0);
        System.out.println(result1.toString());
    }
}
