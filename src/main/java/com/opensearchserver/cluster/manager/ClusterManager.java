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

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensearchserver.cluster.service.ClusterServicesStatusJson;
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

	private final ClusterNodeMap clusterNodeMap;

	private final Set<String> clusterMasterSet;

	private final String myAddress;

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
			isMaster = false;
			return;
		}

		// We load the cluster node map
		clusterNodeMap = new ClusterNodeMap();

		// All is set, let's start the monitoring
		clusterMonitoringThread = new ClusterMonitoringThread(60);
	}

	private ClusterNodeMap checkMaster() {
		if (clusterNodeMap == null)
			throw new JsonApplicationException(Status.NOT_ACCEPTABLE,
					"I am not a master");
		return clusterNodeMap;
	}

	public ClusterNode upsertNode(String address, Set<String> services)
			throws URISyntaxException {
		return checkMaster().upsert(address, services);
	}

	void updateNodeStatus(ClusterNode node) {
		checkMaster().status(node);
	}

	public ClusterNode removeNode(String address) throws URISyntaxException {
		return checkMaster().remove(address);
	}

	public List<ClusterNode> getNodeList() {
		return checkMaster().getNodeList();
	}

	public Set<String> getMasterSet() {
		return clusterMasterSet;
	}

	public boolean isMaster() {
		return isMaster;
	}

	public List<String> getInactiveNodes(String service) {
		List<String> nodeNameList = new ArrayList<String>();
		checkMaster().populateInactive(service, nodeNameList);
		return nodeNameList;
	}

	public List<String> getActiveNodes(String service) {
		List<String> nodeNameList = new ArrayList<String>();
		checkMaster().populateActive(service, nodeNameList);
		return nodeNameList;
	}

	public String getActiveNodeRandom(String service) {
		return checkMaster().getActiveRandom(service);
	}

	public ClusterServicesStatusJson getServicesStatus() {
		return checkMaster().getServiceStatus();
	}
}
