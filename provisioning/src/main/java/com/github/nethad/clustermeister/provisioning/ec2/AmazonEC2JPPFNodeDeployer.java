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
package com.github.nethad.clustermeister.provisioning.ec2;

import com.github.nethad.clustermeister.api.JPPFConstants;
import static com.google.common.base.Preconditions.*;
import com.google.common.util.concurrent.Monitor;
import java.io.InputStream;
import java.util.Properties;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.LoginCredentials;

/**
 * Do not reuse instances of this class.
 *
 * @author daniel
 */
public class AmazonEC2JPPFNodeDeployer extends AmazonEC2JPPFDeployer {
    private static final String ZIP_FILE = "jppf-node.zip";
    private static final String CRC32_FILE = CLUSTERMEISTER_BIN + "/jppf-node-crc-32";
    private static final String JPPF_FOLDER = "/jppf-node/";
    private static final String JPPF_CONFIG_FILE_NAME = "jppf-node.properties";
    private static final String JPPF_CONFIG_FILE_SUBPATH = JPPF_FOLDER + "config/" + JPPF_CONFIG_FILE_NAME;
    private static final String LOG4J_CONFIG_FILE_NAME = "log4j-node.properties";
    private static final String LOG4J_CONFIG_FILE_SUBPATH = JPPF_FOLDER + "config/" + LOG4J_CONFIG_FILE_NAME;
    private static final String START_SCRIPT = "startNode.sh";
    private static final String START_SCRIPT_ARGUMENTS = "jppf-node.properties false true";
    
    public AmazonEC2JPPFNodeDeployer(ComputeServiceContext context,
            NodeMetadata metadata, LoginCredentials credentials,
            AmazonNodeConfiguration nodeConfiguration) {
        super(credentials, context, metadata, nodeConfiguration, ZIP_FILE, 
                CRC32_FILE, JPPF_CONFIG_FILE_SUBPATH, LOG4J_CONFIG_FILE_SUBPATH,
                START_SCRIPT, START_SCRIPT_ARGUMENTS, JPPF_FOLDER);
    }

    @Override
    protected void checkPrecondition() throws Throwable {
        checkState(nodeConfiguration.getDriverAddress() != null, 
                "No driver address set.");
    }

    @Override
    protected Monitor getMonitor() {
        return getNodeMonitor(metadata);
    }

    @Override
    protected Properties getSettings() {
        Properties nodeProperties = new Properties();
        nodeProperties.setProperty(JPPFConstants.RECONNECT_MAX_TIME, "5");
        nodeProperties.setProperty(JPPFConstants.DISCOVERY_ENABLED, "false");
        nodeProperties.setProperty(JPPFConstants.SERVER_HOST, nodeConfiguration.getDriverAddress());
        nodeProperties.setProperty(JPPFConstants.MANAGEMENT_HOST, getPrivateIp());
        nodeProperties.setProperty(JPPFConstants.MANAGEMENT_PORT, 
                String.valueOf(nodeConfiguration.getManagementPort()));
        if(nodeConfiguration.getJvmOptions().isPresent()) {
            nodeProperties.setProperty(JPPFConstants.JVM_OPTIONS, 
                    nodeConfiguration.getJvmOptions().get());
        }
        nodeProperties.setProperty(JPPFConstants.CLASSLOADER_DELEGATION, "parent"); 

        nodeProperties.setProperty(JPPFConstants.PROCESSING_THREADS, 
                String.valueOf(getNumberOfProcessingThreads()));
        return nodeProperties;
    }
}
