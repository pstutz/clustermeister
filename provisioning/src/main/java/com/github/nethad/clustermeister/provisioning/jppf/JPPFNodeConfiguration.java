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
package com.github.nethad.clustermeister.provisioning.jppf;

import com.github.nethad.clustermeister.api.JPPFConstants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @deprecated configurations should be dynamically generated.
 *
 * @author thomas
 */
public class JPPFNodeConfiguration {
    
    Properties properties = new Properties();

    public JPPFNodeConfiguration() {       
        properties.setProperty(JPPFConstants.MANAGEMENT_ENABLED, "true");
        properties.setProperty(JPPFConstants.DISCOVERY_ENABLED, "false");
        properties.setProperty(JPPFConstants.RECONNECT_MAX_TIME, "60");
        properties.setProperty(JPPFConstants.RECONNECT_INTERVAL, "10");
        
        properties.setProperty(JPPFConstants.JVM_OPTIONS, "-Xms64m -Xmx512m -Djava.util.logging.config.file=config/logging-node.properties");
        properties.setProperty(JPPFConstants.CLASSLOADER_DELEGATION, "parent");
    }
    
    public JPPFNodeConfiguration setProperty(String key, String value) {
        properties.setProperty(key, value);
        return this;
    }
    
    public InputStream getPropertyStream() throws IOException {
//        System.out.println("READING CUSTOM CONFIGURATION, inputstream");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        properties.store(baos, "");
        return new ByteArrayInputStream(baos.toByteArray());
    }

	public String getProperty(String key) {
		return properties.getProperty(key);
	}
    
}
