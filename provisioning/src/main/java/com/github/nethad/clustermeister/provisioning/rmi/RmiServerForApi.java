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
package com.github.nethad.clustermeister.provisioning.rmi;

import com.github.nethad.clustermeister.api.NodeInformation;
import com.github.nethad.clustermeister.api.rmi.IRmiServerForApi;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * An RMI server/service for the Clustermeister API to query for currently running nodes.
 * 
 * @author thomas
 */
public class RmiServerForApi implements IRmiServerForApi {
    
    private NodeManager nodeManager;

    /**
     * Returns a collection of currently running nodes.
     * @return 
     */
    @Override
    public Collection<NodeInformation> getAllNodes() {
        // this is apparently necessary, because values() has HashMap.Values in it, and those are not serializable
        Collection<NodeInformation> allNodes = nodeManager.getAllNodes();
        List<NodeInformation> dest = new ArrayList<NodeInformation>(allNodes.size());
        dest.addAll(allNodes);
        return dest;
    }
    
    public void setNodeManager(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }
    
}
