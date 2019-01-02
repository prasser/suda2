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
 * A progress listener for SUDA2
 * 
 * @author Fabian Prasser
 */
public abstract class SUDA2ListenerProgress {

    /**
     * Progress update
     * @param progress Number in [0, 1]
     */
    public abstract void update(double progress);

    /**
     * Overwrite to react on each and every tick
     */
    public void tick() {
        // Empty by design
    }
}
