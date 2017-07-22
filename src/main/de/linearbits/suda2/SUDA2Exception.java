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

public class SUDA2Exception extends RuntimeException {

    /** SVUID*/
    private static final long serialVersionUID = -4149588580657843978L;
    
    /**
     * Creates a new instance
     * @param message
     */
    public SUDA2Exception(String message) {
        super(message);
    }
    
    /**
     * Creates a new instance
     * @param message
     * @param cause
     */
    public SUDA2Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
