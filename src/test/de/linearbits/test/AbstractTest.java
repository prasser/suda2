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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractTest {

    /**
     * Loads data from a file
     * @param file
     * @return
     * @throws IOException 
     */
    protected static int[][] getData(String file) throws IOException {
        List<int[]> rows = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(file)));
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(";");
                int[] row = new int[parts.length];
                for (int i = 0; i < row.length; i++) {
                    row[i] = Integer.valueOf(parts[i]);
                }
                rows.add(row);
                line = reader.readLine();
            }
            return rows.toArray(new int[rows.size()][]);
        } catch (IOException e) {
            throw (e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Prints the data
     * @param data
     */
    protected static void print(int[][] data) {
        for (int[] row : data) {
            System.out.println(" - " + Arrays.toString(row));
        }
    }
}
