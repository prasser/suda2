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
 * Listener for scores discoveries
 * 
 * @author Fabian Prasser
 */
public abstract class SUDA2ListenerScore extends SUDA2ListenerKey {

    /** Intermediate scores */
    private double[]      intermediateScores = null;
    /** Use sdcMicro scores */
    private final boolean sdcMicroScores;
    
    /**
     * Creates a new instance
     * @param sdcMicroScores If set to true, scores will be calculated analogously to sdcMicro
     */
    public SUDA2ListenerScore(boolean sdcMicroScores) {
        this.sdcMicroScores = sdcMicroScores;
    }

    /**
     * A MSU has been discovered
     * 
     * @param row
     * @param size
     */
    public void keyFound(int row, int size) {
        scoreFound(row, size, intermediateScores[size - 1]);
    }

    /**
     * A new score has been discovered
     * 
     * @param row
     * @param size
     * @param score
     */
    public abstract void scoreFound(int row, int size, double score);

    @Override
    void init(int columns, int maxK, int numUniqueRecords, int numDuplicateRecords) {
        this.intermediateScores = new SUDA2Scores(columns, maxK, sdcMicroScores).getIntermediateScores();
    }
}