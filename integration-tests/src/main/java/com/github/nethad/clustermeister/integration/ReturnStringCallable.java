/*
 * Copyright 2012 The Clustermeister Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.nethad.clustermeister.integration;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 *
 * @author thomas
 */
public class ReturnStringCallable implements Callable<String>, Serializable {
    private final String returnString;

    public ReturnStringCallable(String returnString) {
        this.returnString = returnString;
    }

    @Override
    public String call() throws Exception {
        return returnString;
    }
    
}
