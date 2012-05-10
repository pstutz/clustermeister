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
package com.github.nethad.clustermeister.provisioning.torque.commands;

import com.github.nethad.clustermeister.provisioning.CommandLineHandle;
import com.github.nethad.clustermeister.provisioning.CommandRegistry;

/**
 *
 * @author thomas
 */
public class TestCommandLineHandle extends CommandLineHandle {
    
    StringBuilder printed = new StringBuilder();

    public TestCommandLineHandle(CommandRegistry commandRegistry) {
        super(commandRegistry);
    }
    
    @Override
    public void print(String line) {
        printed.append(line).append("\n");
    }
    
    public String getPrintOutput() {
        return printed.toString();
    }
    
}