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

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ComparisonChain;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.LocationScope;

/**
 * Represents a profile configuration list entry.
 * 
 * This class parses and holds AWS EC2 instance configurations.
 *
 * @author daniel
 */
public class AWSInstanceProfile implements Comparable<AWSInstanceProfile> {
    private String profileName;
    private String region;
    private String type;
    private Optional<String> zone = Optional.<String>absent();
    private Optional<String> amiId = Optional.<String>absent();
    private Optional<String> keypairName = Optional.<String>absent();
    private Optional<String> shutdownState = Optional.<String>absent();
    
    /**
     * Create a new {@link AWSInstanceProfile} from AWS EC2 instance meta data.
     * 
     * @param instanceMetadata  the instance meta data.
     * @return a new {@link AWSInstanceProfile} constructed from the instance meta data.
     */
    public static AWSInstanceProfile fromInstanceMetadata(NodeMetadata instanceMetadata) {
        String zoneId = null;
        String regionId;
        if(instanceMetadata.getLocation().getScope() == LocationScope.ZONE) {
            zoneId = instanceMetadata.getLocation().getId();
            regionId = instanceMetadata.getLocation().getParent().getId();
        } else if(instanceMetadata.getLocation().getScope() == LocationScope.REGION) {
            regionId = instanceMetadata.getLocation().getId();
        } else {
            throw new IllegalStateException(String.format(
                    "Instance metadata for '%s' does not contain a region.", 
                    instanceMetadata.getId()));
        }
        
        return newBuilder().
                profileName(String.format("<generated fro %s>", instanceMetadata.getId())).
                region(regionId).
                zone(zoneId).
                type(instanceMetadata.getHardware().getId()).
                amiId(instanceMetadata.getImageId()).
                keypairName(null).
                shutdownState(instanceMetadata.getState().name()).
                build();
        
    }
    
    /**
     * A builder to construct a new {@link AWSInstanceProfile} with an 
     * AMI (Amazon Machine Image) ID.
     * 
     * @see AmiIdBuilder
     * 
     * @return a new builder.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Private Default Constructor.
     * 
     * Only use with Builders.
     */
    private AWSInstanceProfile() {}
    
    /**
     * Returns the configured name for this profile.
     * 
     * This value is mandatory.
     * 
     * @return the profile name. 
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Returns the AMI (Amazon Machine Image) ID configured for this profile.
     * 
     * @return the AMI ID.
     */
    public Optional<String> getAmiId() {
        return amiId;
    }
    
    /**
     * Returns the AWS region ID configured for this profile.
     * 
     * This value is mandatory.
     * 
     * @return the AWS region ID (e.g. eu-west-1).
     */
    public String getRegion() {
        return region;
    }
    
    /**
     * Returns the AWS Availability Zone configured for this profile.
     * 
     * @return the AWS availability zone (e.g. eu-west-1a).
     */
    public Optional<String> getZone() {
        return zone;
    }
    
    /**
     * Returns the AWS hardware type configured for this profile.
     * 
     * @return the AWS hardware type (e.g. t1.micro). 
     * 
     * @see 
     *      <a href="http://aws.amazon.com/ec2/instance-types/">
     *      Amazon EC2 Instance Types</a>
     */
    public String getType() {
        return type;
    }
    
    /**
     * Returns the keypair name configured for this profile.
     * 
     * @return the keypair name (referencing an entry in the keypairs configuration). 
     */
    public Optional<String> getKeyPairName() {
        return keypairName;
    }
    
    /**
     * Returns the shutdown state configured for this profile.
     * 
     * @return 
     *      the state to put the instance in when it is shut down 
     *      (terminated|suspended|running). 
     */
    public Optional<String> getShutdownState() {
        return shutdownState;
    }
    
    @Override
    public String toString() {
        ToStringHelper helper = Objects.toStringHelper(profileName).
                add("Region", region);
        if(zone.isPresent()) {
            helper.add("Zone", zone.get());
        }
        if(amiId.isPresent()) {
            helper.add("AMI ID", amiId.get());
        }
        helper.add("Type", type);
        
        if(keypairName.isPresent()) {
            helper.add("Keypair", keypairName.get());
        }
        
        if(shutdownState.isPresent()) {
            helper.add("Shutdown State", shutdownState.get());
        }
        
        return helper.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != (getClass())) {
            return false;
        }
        AWSInstanceProfile otherProfile = (AWSInstanceProfile) obj;
        return new EqualsBuilder().
                append(profileName, otherProfile.profileName).
                isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(profileName);
    }

    @Override
    public int compareTo(AWSInstanceProfile that) {
        return ComparisonChain.start().
                compare(this.profileName, that.profileName).
                result();
    }
    
    /**
     * Builds new {@link AWSInstanceProfile}.
     * 
     * <p>
     * This builder requires mandatory configuration of:
     * <ul>
     * <li>profile name</li>
     * <li>region</li>
     * <li>type</li>
     * </ul>
     * </p>
     */
    public static class Builder {
        /**
         * Default message for {@link #checkString(java.lang.String, java.lang.String, java.lang.Object[])}
         * when checking a key value pair.
         */
        protected static final String KEY_VALUE_MESSAGE = "Invalid %s for profile '%s'.";
        
        /**
         * The profile to set values on.
         */
        protected AWSInstanceProfile profile = new AWSInstanceProfile();
        
        /**
         * Build a new {@link AWSInstanceProfile} from the previously set values.
         * 
         * @return 
         *      the new {@link AWSInstanceProfile} if contains all mandatory 
         *      values for this builder.
         */
        public AWSInstanceProfile build() {
            this.profile.profileName = checkString(
                    this.profile.profileName, "Invalid profile name.");
            this.profile.region = checkString(
                    this.profile.region, KEY_VALUE_MESSAGE, 
                    AmazonConfigurationLoader.REGION, this.profile.profileName);
            this.profile.type = checkString(
                    this.profile.type, KEY_VALUE_MESSAGE, 
                    AmazonConfigurationLoader.TYPE, this.profile.profileName);
            
            if(this.profile.amiId.isPresent()) {
                this.profile.amiId = Optional.of(checkString(
                        this.profile.amiId.get(), KEY_VALUE_MESSAGE, 
                        AmazonConfigurationLoader.AMI_ID, this.profile.profileName));
                
            }
            
            if(this.profile.zone.isPresent()) {
                this.profile.zone = Optional.of(checkString(
                        this.profile.zone.get(), KEY_VALUE_MESSAGE, 
                        AmazonConfigurationLoader.ZONE, this.profile.profileName));
            }
            
            if(this.profile.keypairName.isPresent()) {
                this.profile.keypairName = Optional.of(checkString(
                        this.profile.keypairName.get(), KEY_VALUE_MESSAGE, 
                        AmazonConfigurationLoader.KEYPAIR, this.profile.profileName));
            }
            
            if(this.profile.shutdownState.isPresent()) {
                this.profile.shutdownState = Optional.of(checkString(
                        this.profile.shutdownState.get(), KEY_VALUE_MESSAGE, 
                        AmazonConfigurationLoader.SHUTDOWN_STATE, this.profile.profileName));
                try {
                    String shutdownStateStr = this.profile.shutdownState.get().toUpperCase();
                    AmazonInstanceShutdownState.valueOf(shutdownStateStr);
                } catch(IllegalArgumentException ex) {
                    throw new IllegalArgumentException(
                            String.format(KEY_VALUE_MESSAGE, 
                            AmazonConfigurationLoader.SHUTDOWN_STATE, 
                            this.profile.profileName), ex);
                }
            }
            
            return profile;
        }
        
        /**
         * Set a profile name.
         * 
         * @param profileName 
         *      the name should be unique and is used to refer to this profile.
         * @return this instance for chaining.
         */
        public Builder profileName(String profileName) {
            this.profile.profileName = profileName;
            return this;
        }
        
        /**
         * Set an AMI (Amazon Machine Image) ID.
         * 
         * @param amiId the AMI ID (e.g. ami-f9231b8d).
         * @return this instance for chaining.
         */
        public Builder amiId(String amiId) {
            this.profile.amiId = Optional.fromNullable(amiId);
            return this;
        }
        
        /**
         * Set an AWS Region ID.
         * 
         * @param region the AWS region ID (e.g. eu-west-1).
         * @return this instance for chaining.
         */
        public Builder region(String region) {
            this.profile.region = region;
            return this;
        }
        
        /**
         * Sets an AWS Availability Zone.
         * 
         * @param zone the AWS availability zone (e.g. eu-west-1a).
         * @return this instance for chaining.
         */
        public Builder zone(String zone) {
            this.profile.zone = Optional.fromNullable(zone);
            return this;
        }
        
        /**
         * Sets an AWS hardware type.
         * 
         * @param type the AWS hardware type API name (e.g. t1.micro).
         * @return this instance for chaining.
         */
        public Builder type(String type) {
            this.profile.type = type;
            return this;
        }
        
        /**
         * Sets a keypair name.
         * 
         * @param keypairName 
         *      the keypair name (referencing a keypair configured in the 
         *      keypairs configuration).
         * @return this instance for chaining.
         */
        public Builder keypairName(String keypairName) {
            this.profile.keypairName = Optional.fromNullable(keypairName);
            return this;
        }
        
        /**
         * Sets a shutdown state.
         * 
         * @param keypairName 
         *      the state to put the instance in when it is shut down 
         *      (terminated|suspended|running).
         * @return this instance for chaining.
         */
        public Builder shutdownState(String shutdownState) {
            this.profile.shutdownState = Optional.fromNullable(shutdownState);
            return this;
        }
        
        /**
         * Check a string reference for being non-null and not empty.
         * 
         * Throws {@link IllegalArgumentException} or {@link NullPointerException} 
         * if the string reference does not meet the criteria.
         * 
         * @param string    the string reference to check.
         * @param message   error message pattern
         * @param messageArgs arguments to fill into the message patterns (replacing %s).
         * 
         * @return the trimmed String.
         */
        protected String checkString(String string, String message, Object... messageArgs) {
            checkNotNull(string, message, messageArgs);
            String trimmed = string.trim();
            checkArgument(!trimmed.isEmpty(), message, messageArgs);
            
            return trimmed;
        }
    }
}