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
package com.github.nethad.clustermeister.provisioning.cli;

import com.github.nethad.clustermeister.api.Loggers;
import com.github.nethad.clustermeister.api.impl.YamlConfiguration;
import com.github.nethad.clustermeister.provisioning.CommandLineArguments;
import com.github.nethad.clustermeister.provisioning.CommandLineEvaluation;
import com.github.nethad.clustermeister.provisioning.CommandLineHandle;
import com.github.nethad.clustermeister.provisioning.CommandRegistry;
import com.github.nethad.clustermeister.provisioning.ec2.AmazonNodeManager;
import com.github.nethad.clustermeister.provisioning.jppf.JPPFLocalDriver;
import com.github.nethad.clustermeister.provisioning.rmi.RmiInfrastructure;
import com.github.nethad.clustermeister.provisioning.torque.TorqueNodeManager;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class Provisioning {
    private Logger logger = LoggerFactory.getLogger(Loggers.CLI);
//    @Inject
//    @Named(Loggers.CLI)
//    private Logger logger;
    
    private CommandLineEvaluation commandLineEvaluation;
    private String configFilePath;
//    private String driverHost;
    private Provider provider;
    private Configuration configuration;
    private RmiInfrastructure rmiInfrastructure;
    private JPPFLocalDriver jppfLocalDriver;
    private CommandLineHandle commandLineHandle;

    public Provisioning(String configFilePath, Provider provider, CommandRegistry commandRegistry) {
        this.configFilePath = configFilePath;
        this.provider = provider;
        rmiInfrastructure = new RmiInfrastructure();
        rmiInfrastructure.initialize();
        commandLineHandle = new CommandLineHandle(commandRegistry);
    }
    
    public void execute() {
        readConfigFile();
        switch(provider) {
            case AMAZON:
                startAmazon();
                break;
            case TORQUE:
                startTorque();
                break;
            case TEST:
                startTestSetup();
                break;
            default:
                throw new RuntimeException("Unknown provider");
        }
    }
    
    public void commandShutdown(CommandLineArguments arguments) {
        commandLineEvaluation.shutdown(arguments);
        shutdownDriver();
    }   
    
    public void commandUnknownFallback(String command, CommandLineArguments arguments) {
        commandLineEvaluation.handleCommand(command, arguments);
    }
    
    public void commandState(CommandLineArguments arguments) {
        commandLineEvaluation.state(arguments);
    }
    
    protected Provider getProvider() {
        return provider;
    }
    
    private void readConfigFile() {
        if (configFilePath == null || !(new File(configFilePath).exists())) {
            logger.warn("Configuration file \""+configFilePath+"\" does not exist.");
        } else {
            try {
                configuration = new YamlConfiguration(configFilePath);
            } catch (ConfigurationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void shutdownDriver() {
        jppfLocalDriver.shutdown();
    }

    private void startAmazon() {
        commandLineEvaluation = AmazonNodeManager.commandLineEvaluation(configuration, commandLineHandle, rmiInfrastructure);
        startLocalDriver();
    }

    private void startLocalDriver() {
        jppfLocalDriver = new JPPFLocalDriver(configuration);
        jppfLocalDriver.execute();
    }

    private void startTorque() {
        jppfLocalDriver = new JPPFLocalDriver(configuration);
        commandLineEvaluation = TorqueNodeManager.commandLineEvaluation(configuration, commandLineHandle, jppfLocalDriver, rmiInfrastructure.getRmiServerForApiObject());
        jppfLocalDriver.execute();
    }
    
    private void startTestSetup() {
        commandLineEvaluation = new CommandLineEvaluation() {
            @Override
            public void state(CommandLineArguments arguments) {} // do nothing
            @Override
            public void shutdown(CommandLineArguments arguments) {} // do nothing
            @Override
            public void handleCommand(String command, CommandLineArguments arguments) {} // do nothing
            @Override
            public CommandLineHandle getCommandLineHandle() { return null; }
        };
        startLocalDriver();
        jppfLocalDriver.update(null, "127.0.0.1");
    }

    @VisibleForTesting
    public RmiInfrastructure getRmiInfrastructure() {
        return rmiInfrastructure;
    }

    @VisibleForTesting
    void setCommandLineEvaluation(CommandLineEvaluation evaluation) {
        this.commandLineEvaluation = evaluation;
    }

    
}
