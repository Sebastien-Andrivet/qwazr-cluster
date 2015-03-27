/**
 * Copyright 2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opensearchserver.cluster.manager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensearchserver.utils.json.JsonApplicationException;
import com.opensearchserver.utils.server.AbstractServer;

public class ClusterManager {

	private static final Logger logger = LoggerFactory
			.getLogger(ClusterManager.class);

	public static volatile ClusterManager INSTANCE = null;

	public static void load(AbstractServer server, File directory)
			throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		try {
			INSTANCE = new ClusterManager(server, directory);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public static final String MASTER_SERVICE_NAME = "master".intern();

	public static final String CLUSTER_CONFIGURATION_NAME = "cluster.yaml";

	private static final String CLUSTER_CONF_PATH = System
			.getProperty("com.opensearchserver.cluster.conf");

	private final ConcurrentHashMap<String, ClusterNode> clusterNodeMap;

	private final ClusterNodeByServiceMap nodesByServiceMap;

	private final Set<String> clusterMasterSet;

	private final String myAddress;

	private List<ClusterNode> clusterNodeList;

	private final ClusterMonitoringThread clusterMonitoringThread;

	private final boolean isMaster;

	protected int port;

	private ClusterManager(AbstractServer server, File rootDirectory)
			throws IOException, URISyntaxException {
		this.port = server.getRestTcpPort();
		myAddress = ClusterNode.toAddress(server.getCurrentHostname(), port);
		logger.info("Server: " + myAddress);

		// Look for the configuration file
		File clusterConfigurationFile = CLUSTER_CONF_PATH != null ? new File(
				CLUSTER_CONF_PATH) : new File(rootDirectory,
				CLUSTER_CONFIGURATION_NAME);
		ClusterConfiguration clusterConfiguration = ClusterConfiguration
				.newInstance(clusterConfigurationFile);

		// No configuration file ? Okay, we are a simple node
		if (clusterConfiguration == null)
			throw new IOException("The configuration file is missing: "
					+ CLUSTER_CONFIGURATION_NAME);

		// Load the configuration file
		if (clusterConfiguration.masters == null
				|| clusterConfiguration.masters.isEmpty())
			throw new IOException(
					"No masters configured in the configuration file: "
							+ CLUSTER_CONFIGURATION_NAME);

		// Build the master list and check if I am a master
		boolean isMaster = false;
		clusterMasterSet = new HashSet<String>();
		for (String master : clusterConfiguration.masters) {
			String address = ClusterNode.toAddress(master, null);
			logger.info("Add a master: " + address);
			clusterMasterSet.add(address);
			if (address == myAddress) {
				isMaster = true;
				logger.info("I am a master!");
			}
		}
		this.isMaster = isMaster;
		if (!isMaster) {
			clusterNodeMap = null;
			clusterMonitoringThread = null;
			nodesByServiceMap = null;
			isMaster = false;
			return;
		}

		// We load the cluster node map
		clusterNodeMap = new ConcurrentHashMap<String, ClusterNode>();
		nodesByServiceMap = new ClusterNodeByServiceMap();
		buildNodeList();

		// All is set, let's start the monitoring
		clusterMonitoringThread = new ClusterMonitoringThread(60);
	}

	private void buildNodeList() {
		List<ClusterNode> newClusterNodeList = new ArrayList<ClusterNode>(
				clusterNodeMap.size());
		for (ClusterNode clusterNode : clusterNodeMap.values())
			newClusterNodeList.add(clusterNode);
		clusterNodeList = newClusterNodeList;
	}

	public ClusterNode setClusterNode(String address, Set<String> services)
			throws URISyntaxException {
		if (clusterNodeMap == null || nodesByServiceMap == null)
			throw new JsonApplicationException(Status.NOT_ACCEPTABLE,
					"I am not a master");
		ClusterNode newClusterNode = new ClusterNode(address, services);
		ClusterNode oldClusterNode = clusterNodeMap.put(newClusterNode.address,
				newClusterNode);
		if (oldClusterNode != null)
			nodesByServiceMap.remove(oldClusterNode);
		nodesByServiceMap.insert(newClusterNode);
		buildNodeList();
		return newClusterNode;
	}

	public ClusterNode removeClusterNode(String address)
			throws URISyntaxException {
		if (clusterNodeMap == null || nodesByServiceMap == null)
			throw new JsonApplicationException(Status.NOT_ACCEPTABLE,
					"I am not a master");
		address = ClusterNode.toAddress(address, null);
		ClusterNode clusterNode = clusterNodeMap.remove(address);
		if (clusterNode == null)
			return null;
		nodesByServiceMap.remove(clusterNode);
		return clusterNode;
	}

	public List<ClusterNode> getClusterNodeList() {
		if (clusterNodeList == null)
			throw new JsonApplicationException(Status.NOT_ACCEPTABLE,
					"I am not a master");
		return clusterNodeList;
	}

	public boolean isMaster() {
		return isMaster;
	}

}
