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
     * Prints the data
     * @param data
     */
    protected static void print(int[][] data) {
        for (int[] row : data) {
            System.out.println(" - " + Arrays.toString(row));
        }
    }

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
}
