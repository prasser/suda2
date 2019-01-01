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

import java.io.IOException;

import de.linearbits.suda2.SUDA2;
import de.linearbits.suda2.SUDA2Statistics;
import de.linearbits.suda2.Timeable;

/**
 * Test based on survey data data
 * 
 * @author Fabian Prasser
 */
public class Test9 extends AbstractTest{

    /**
     * Main entry point
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
       
        // As array
        String[] files = new String[]{
            "data/test.csv",    // Adult
            "data/test2.csv",   // Whatever
            "data/test3.csv",   // Whatever
            "data/test4.csv",   // FARS
            "data/test7.csv",   // CUP
            "data/test5.csv",   // IHIS
            "data/test6.csv"    // SS13ACS
        };
        
        for (String file : files) {
            int[][] dataset = getData(file);
            System.out.println("Dataset: " + file + " length: " + dataset.length);
            // Process
            int REPETITIONS = 5;
            long time = System.currentTimeMillis();
            for (int i=0; i<REPETITIONS; i++) {
                long time2 = System.currentTimeMillis();
                Timeable.reset();
                SUDA2Statistics stats = new SUDA2(dataset).getKeyStatistics(0, true);
                System.out.println(" Run: " + (System.currentTimeMillis() - time2) + " MSUs: " + stats.getNumKeys());
            }
            time = (long)((System.currentTimeMillis() - time) / (double)REPETITIONS);
            System.out.println(" - Average time: " + time);
            // Timeable.printOverview();
        }
    }
}
