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

import java.util.Random;

import com.carrotsearch.hppc.IntOpenHashSet;

/**
 * Compare implementations of integer sets
 * 
 * @author Fabian Prasser
 */
public class TestIntegerSets {

    public static void main(String[] args) {
        
        // Large sets
        Random random = new Random(0xDEADBEEF);
        long time = System.currentTimeMillis();
        for (int r=0; r<100; r++) {
            SUDA2IntSet set = new SUDA2IntSet();
            for (int k=0; k<1000000; k++) {
                set.add(random.nextInt() + 1);
            }
        }
        System.out.println("[Large] OWN: " + (System.currentTimeMillis() - time));

        random = new Random(0xDEADBEEF);
        time = System.currentTimeMillis();
        for (int r=0; r<100; r++) {
            IntOpenHashSet set = new IntOpenHashSet();
            for (int k=0; k<1000000; k++) {
                set.add(random.nextInt() + 1);
            }
        }
        System.out.println("[Large] HPPC: " + (System.currentTimeMillis() - time));
        
        // Small sets
        random = new Random(0xDEADBEEF);
        time = System.currentTimeMillis();
        for (int r=0; r<100000; r++) {
            SUDA2IntSet set = new SUDA2IntSet();
            for (int k=0; k<1000; k++) {
                set.add(random.nextInt() + 1);
            }
        }
        System.out.println("[Small] OWN: " + (System.currentTimeMillis() - time));

        random = new Random(0xDEADBEEF);
        time = System.currentTimeMillis();
        for (int r=0; r<100000; r++) {
            IntOpenHashSet set = new IntOpenHashSet();
            for (int k=0; k<1000; k++) {
                set.add(random.nextInt() + 1);
            }
        }
        System.out.println("[Small] HPPC: " + (System.currentTimeMillis() - time));
    }
}
