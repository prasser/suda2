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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Compare implementations of integer sets
 * 
 * @author Fabian Prasser
 */
public class TestSetImplementations {

    public static void main(String[] args) {
        
        Random random = new Random();
        for (int i=0; i< 1000000; i++) {
            
            int num = random.nextInt(11);
            
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            Set<Integer> elements = new HashSet<Integer>();
            for (int j=0; j<num; j++) {
                int number = random.nextInt(256)+1;
                min = Math.min(number, min);
                max = Math.max(number, max);
                elements.add(number);
            }
            
            SUDA2IntSetBits set1 = new SUDA2IntSetBits(min, max);
            for (int element : elements) {
                set1.add(element);
            }
            
            for (int j=0; j<num; j++) {
                int number = random.nextInt(256)+1;
                min = Math.min(number, min);
                max = Math.max(number, max);
                elements.add(number);
            }
            SUDA2IntSetBits set2 = new SUDA2IntSetBits(min, max);
            for (int element : elements) {
                set2.add(element);
            }
            
            SUDA2IntSet intersect1 = set1.intersectWith(set2);
            if (intersect1.size() != set1.size()) {
                System.out.println(set1.toString());
                System.out.println(set2.toString());
                System.out.println(intersect1.toString());
                throw new IllegalStateException();
            }
            SUDA2IntSet intersect2 = set2.intersectWith(set1);
            if (intersect2.size() != set1.size()) {
                System.out.println(set1.toString());
                System.out.println(set2.toString());
                System.out.println(intersect2.toString());
                throw new IllegalStateException();
            }

            System.out.println(set1);
            System.out.println(set2);
            System.out.println(intersect1);
            System.out.println(intersect2);
            System.out.println("-----------");
            
        }
    }
}
