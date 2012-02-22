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

import com.github.nethad.clustermeister.api.Node;
import com.github.nethad.clustermeister.api.NodeType;
import com.google.common.base.Objects;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jclouds.compute.domain.NodeMetadata;

/**
 *
 * @author daniel
 */
public class AmazonNode implements Node {
	final String id;
	final NodeType type;
	NodeMetadata instanceMetadata;

	public AmazonNode(String uuid, NodeType type, NodeMetadata instanceMetadata) {
		this.instanceMetadata = instanceMetadata;
		this.type = type;
		this.id = uuid;
	}
	
	void updateInstanceMetaData(NodeMetadata instanceMetadata) {
		this.instanceMetadata = instanceMetadata;
	}
	
	NodeMetadata getInstanceMetadata() {
		return instanceMetadata;
	}
	
	public String getState() {
		return instanceMetadata.getState().toString();
	}

	public String getInstanceId() {
		return instanceMetadata.getId();
	}
	
	@Override
	public String getID() {
		return id;
	}

	@Override
	public NodeType getType() {
		return type;
	}

	@Override
	public Set<String> getPrivateAddresses() {
		return instanceMetadata.getPrivateAddresses();
	}
	
	@Override
	public Set<String> getPublicAddresses() {
		return instanceMetadata.getPublicAddresses();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
				append(type).
				append(id).
				append(instanceMetadata.getId()).
				append(instanceMetadata.getState()).
				append("public", instanceMetadata.getPublicAddresses()).
				append("private", instanceMetadata.getPrivateAddresses()).
				toString();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(obj == this) {
			return true;
		}
		if(obj.getClass() != (getClass())) {
			return false;
		}
		AmazonNode otherNode = (AmazonNode) obj;
		return new EqualsBuilder().
				append(id, otherNode.id).
				isEquals();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
