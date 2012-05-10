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
package com.github.nethad.clustermeister.provisioning.ec2.commands;

import com.github.nethad.clustermeister.api.NodeCapabilities;
import com.github.nethad.clustermeister.api.NodeType;
import com.github.nethad.clustermeister.provisioning.CommandLineArguments;
import com.github.nethad.clustermeister.provisioning.ec2.AmazonCommandLineEvaluation;
import com.github.nethad.clustermeister.provisioning.ec2.AmazonNodeConfiguration;
import com.github.nethad.clustermeister.provisioning.ec2.AmazonNodeManager;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author daniel
 */
public class AddNodesCommand extends AbstractAmazonExecutableCommand {
    
    private static final String[] ARGUMENTS = 
            new String[]{"number of nodes", "processing threads per node"};

    private static final String HELP_TEXT = "Add nodes to the cluster.";
    
    private static final String NAME = "addnodes";
    
    
    public AddNodesCommand(AmazonCommandLineEvaluation commandLineEvaluation) {
        super(NAME, ARGUMENTS, HELP_TEXT, commandLineEvaluation);
    }

    @Override
    public void execute(CommandLineArguments arguments) {
        AmazonNodeManager nodeManager = getNodeManager();
        
        if (isArgumentsCountFalse(arguments)) {
            return;
        }
        
        Scanner scanner = arguments.asScanner();
        
        final int numberOfNodes = scanner.nextInt();
        final int numberOfCpusPerNode = scanner.nextInt();
                final AmazonNodeConfiguration amazonNodeConfiguration = new AmazonNodeConfiguration();
        amazonNodeConfiguration.setDriverAddress("localhost");
        amazonNodeConfiguration.setNodeCapabilities(new NodeCapabilities() {
            @Override
            public int getNumberOfProcessors() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getNumberOfProcessingThreads() {
                return numberOfCpusPerNode;
            }

            @Override
            public String getJppfConfig() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        amazonNodeConfiguration.setNodeType(NodeType.NODE);
        amazonNodeConfiguration.setRegion("eu-west-1c");
        
        logger.info("Starting {} nodes.", numberOfNodes);
        List<ListenableFuture<? extends Object>> futures = 
                new ArrayList<ListenableFuture<? extends Object>>(numberOfNodes);
        for (int i = 0; i < numberOfNodes; i++) {
            futures.add(nodeManager.addNode(amazonNodeConfiguration, 
                    Optional.<String>absent()));
        }
        waitForFuturesToComplete(futures, 
                "Interrupted while waiting for nodes to start. Nodes may not be started properly.", 
                "Failed to wait for nodes to start.", "{} nodes failed to start.");
    }
    
}