package de.linearbits.test;

import java.util.Arrays;

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
}
