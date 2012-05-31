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
package com.github.nethad.clustermeister.provisioning.local;

import com.github.nethad.clustermeister.api.JPPFConstants;
import com.github.nethad.clustermeister.provisioning.jppf.JPPFNodeConfiguration;
import com.google.common.io.Files;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents and deploys a Clustermeister node running on the local machine (same machine as 
 * the CLI) and connects to a JPPF Driver on localhost.
 *
 * @author thomas, daniel
 */
public class JPPFLocalNode {
    private static final Logger logger = 
            LoggerFactory.getLogger(JPPFLocalNode.class);

    /**
     * The specification of this node.
     */
    protected final LocalNodeConfiguration nodeConfiguration;
    
    private File targetDir;
    
    private int managementPort = 12001;
    private int currentNodeNumber = 0;
    private String currentNodeConfig;
    private File libDir;

    /**
     * Create a new local node with a node configuration.
     * 
     * @param nodeConfiguration the node configuration.
     */
    public JPPFLocalNode(LocalNodeConfiguration nodeConfiguration) {
        this.nodeConfiguration = nodeConfiguration;
    }
    
    /**
     * Deploy a new local node.
     */
    public void deploy() {
        prepare();
        startNewNode();
    }
    
    /**
     * Delete the node's directory after shutdown.
     */
    public void cleanupAfterShutdown() {
        try {
            FileUtils.deleteDirectory(targetDir);
        } catch (Exception ex) {
            logger.warn("Could not delete the node's directory.", ex);
        }
    }
    
    /**
     * Unpack the JPPF node to its auto-generated directory 
     * and deploy preload artifacts.
     */
    protected void prepare() {
        unpackNodeZip();
        libDir = new File(targetDir, "jppf-node/lib/");
        preloadLibraries(nodeConfiguration.getArtifactsToPreload());
    }
    
    /**
     * Deploy specified files to the node's lib directory.
     * 
     * @param artifactsToPreload the artifacts to deploy.
     */
    protected void preloadLibraries(Collection<File> artifactsToPreload) {
        for (File artifact : artifactsToPreload) {
            File destinationFile = new File(libDir, artifact.getName());
            try {
                logger.info("Copy {} to {}", artifact.getName(), 
                        destinationFile.getAbsolutePath());
                Files.copy(artifact, destinationFile);
            } catch (IOException ex) {
                logger.warn("Could not copy artifact {} to {}", 
                        new Object[]{artifact.getAbsolutePath(), 
                        destinationFile.getAbsolutePath(), ex});
            }
        }
    }
    
    /**
     * Generate and deploy configuration files and start the node process.
     */
    protected void startNewNode() {
        prepareNodeConfiguration();
        startNode();
    }

    /**
     * Generate and deploy configuration files.
     * 
     * @throws RuntimeException when there's an IOException.
     */
    protected void prepareNodeConfiguration() throws RuntimeException {
        JPPFNodeConfiguration nodeConfiguration = new JPPFNodeConfiguration();
        nodeConfiguration.setProperty(JPPFConstants.SERVER_HOST, "localhost")
                .setProperty(JPPFConstants.MANAGEMENT_PORT, String.valueOf(managementPort++))
                .setProperty(JPPFConstants.RESOURCE_CACHE_DIR, "/tmp/.jppf/node-" + System.currentTimeMillis())
                .setProperty(JPPFConstants.PROCESSING_THREADS, String.valueOf(this.nodeConfiguration.getNumberOfProcessingThreads()));
        
        currentNodeConfig = "jppf-node-"+(currentNodeNumber++)+".properties";
        File configDir = new File(targetDir, "/jppf-node/config/");
        final File propertiesFile = new File(configDir, currentNodeConfig);
        try {
            InputStream propertyStream = nodeConfiguration.getPropertyStream();
            copyInputStream(propertyStream, new FileOutputStream(propertiesFile));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Start the node in a new independent process.
     * 
     * @throws RuntimeException when there's an IOException.
     */
    protected void startNode() throws RuntimeException {
        File startNodeScript = new File(targetDir, "jppf-node/startNode.sh");
        startNodeScript.setExecutable(true);
        try {
            //            String jppfNodePath = startNodeScript.getParentFile().getAbsolutePath();
            String jvmOptions = nodeConfiguration.getJvmOptions().or("").replaceAll("\\s", "\\ ");
            System.out.println("jvmOptions = "+jvmOptions);
            
            final String command = String.format("%s %s %s %s %s",
                    "./" + startNodeScript.getName(), 
                    currentNodeConfig,
                    "false", "false", jvmOptions);
            logger.info("Start node with {}", command);
            Runtime.getRuntime().exec(command, new String[]{}, startNodeScript.getParentFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void unpackNodeZip() throws RuntimeException {
        targetDir = Files.createTempDir();
        logger.info("Created temp dir {}", targetDir.getAbsolutePath());
        InputStream jppfNodeZipStream = JPPFLocalNode.class.getResourceAsStream("/jppf-node.zip");
        if (jppfNodeZipStream == null) {
            throw new RuntimeException("Could not find jppf-node.zip.");
        }
        unzipNode(jppfNodeZipStream, targetDir);
    }
    
    //TODO: probabaly worthy to use truezip here but beware of license (EPL)
    private void unzipNode(InputStream fileToUnzip, File targetDir) {
        ZipInputStream zipFile;
        try {
            zipFile = new ZipInputStream(fileToUnzip);
            ZipEntry entry;
            while ((entry = zipFile.getNextEntry()) != null) {
//                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then children.
                    System.err.println("Extracting directory: " + entry.getName());
                    // This is not robust, just for demonstration purposes.
                    (new File(targetDir, entry.getName())).mkdir();
                    continue;
                }
                System.err.println("Extracting file: " + entry.getName());
                File targetFile = new File(targetDir, entry.getName());
                copyInputStream_notClosing(zipFile,
                        new BufferedOutputStream(new FileOutputStream(targetFile)));
//                zipFile.closeEntry();
            }
            zipFile.close();
        } catch (IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
        }
    }
    
    private void copyInputStream_notClosing(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        out.close();
    }
        
    public void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }
}
