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
package com.github.nethad.clustermeister.provisioning.jppf.managementtasks;

import com.github.nethad.clustermeister.provisioning.utils.NodeManagementConnector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.jppf.management.JMXNodeConnectionWrapper;
import org.jppf.server.protocol.JPPFRunnable;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class JPPFShutdownTask implements Serializable {
    
    private final static org.slf4j.Logger logger =
            LoggerFactory.getLogger(JPPFShutdownTask.class);

    @JPPFRunnable
    public void shutdownNodes(List<String> sockets, int myPort) throws TimeoutException, Exception {
        List<Thread> runningThreads = startShutdownTaskThreads(sockets, myPort);

        waitForAllThreadsToComplete(runningThreads);
        shutdownThisRunningNode(myPort);
    }

    private void shutdownThisRunningNode(int myPort) throws Exception, TimeoutException {
        logger.debug("Shutting down myself...");
        JMXNodeConnectionWrapper wrapper = 
                NodeManagementConnector.openNodeConnection("localhost", myPort);
        wrapper.shutdown();
    }

    private void waitForAllThreadsToComplete(List<Thread> runningThreads) {
        for (Thread thread : runningThreads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                continue;
            }
        }
    }

    private List<Thread> startShutdownTaskThreads(List<String> sockets, int myPort) 
            throws NumberFormatException {
        List<Thread> runningThreads = new ArrayList<Thread>();
        for (String socket : sockets) {
            String[] split = socket.split(":");
            String ip = split[0];
            int port = Integer.parseInt(split[1]);

            if (port != myPort) {
                Thread t = new ShutdownTaskThread(ip, port);
                runningThreads.add(t);
                t.start();
            }
        }
        return runningThreads;
    }

    private class ShutdownTaskThread extends Thread {

        private final String ip;
        private final int port;

        public ShutdownTaskThread(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                logger.debug("Shutting down {}:{}.", ip, port);
                JMXNodeConnectionWrapper wrapper = 
                        NodeManagementConnector.openNodeConnection(ip, port);
                wrapper.shutdown();
                wrapper.close();
            } catch (TimeoutException ex) {
                logger.warn("Timed out waiting for node management connection.", ex);
            } catch (Exception ex) {
                logger.warn("Could not shut down {}:{}. {}", new Object[]{ip, port, ex.getMessage()});
            }
        }
    }
}
