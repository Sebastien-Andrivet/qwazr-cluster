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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensearchserver.cluster.ClusterClient;
import com.opensearchserver.cluster.manager.ClusterNodeSet.Cache;
import com.opensearchserver.cluster.service.ClusterNodeRegisterJson;
import com.opensearchserver.cluster.service.ClusterNodeStatusJson;
import com.opensearchserver.cluster.service.ClusterServiceStatusJson;
import com.opensearchserver.cluster.service.ClusterServiceStatusJson.StatusEnum;
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
			if (INSTANCE.isMaster()) {
				INSTANCE.loadNodesFromOtherMaster();
				INSTANCE.startMonitoringThread();
			}
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

	private ClusterMonitoringThread clusterMonitoringThread = null;

	private Thread clusterNodeShutdownThread = null;

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
		if (clusterConfiguration == null
				|| clusterConfiguration.masters == null
				|| clusterConfiguration.masters.isEmpty()) {
			clusterMasterSet = null;
			clusterNodeMap = null;
			clusterMonitoringThread = null;
			isMaster = false;
			logger.info("No cluster configuration. This node is not part of a cluster.");
			return;
		}

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
			isMaster = false;
			return;
		}

		// We load the cluster node map
		clusterNodeMap = new ClusterNodeMap();
	}

	private void loadNodesFromOtherMaster() {
		// Let's try to load the other nodes from another master
		for (String master : clusterMasterSet) {
			if (master == myAddress)
				continue;
			try {
				logger.warn("Get node list from  " + master);
				Map<String, Set<String>> nodesMap = new ClusterClient(master,
						60000).getNodes();
				if (nodesMap == null)
					continue;
				for (Map.Entry<String, Set<String>> entry : nodesMap.entrySet())
					upsertNode(entry.getKey(), entry.getValue());
				break;
			} catch (Exception e) {
				logger.warn("Unable to load the node list from " + master, e);
			}
		}
	}

	private void startMonitoringThread() {
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

	private List<String> buildList(ClusterNode[] nodes) {
		if (nodes == null)
			return ClusterServiceStatusJson.EMPTY_LIST;
		List<String> nodeNameList = new ArrayList<String>();
		for (ClusterNode node : nodes)
			nodeNameList.add(node.address);
		return nodeNameList;
	}

	private Cache getNodeSetCache(String service) {
		ClusterNodeSet nodeSet = checkMaster().getNodeSet(service);
		if (nodeSet == null)
			return null;
		return nodeSet.getCache();
	}

	public List<String> getInactiveNodes(String service) {
		Cache cache = getNodeSetCache(service);
		if (cache == null)
			return ClusterServiceStatusJson.EMPTY_LIST;
		return buildList(cache.activeArray);
	}

	public List<String> getActiveNodes(String service) {
		Cache cache = getNodeSetCache(service);
		if (cache == null)
			return ClusterServiceStatusJson.EMPTY_LIST;
		return buildList(cache.activeArray);
	}

	/**
	 * @param service
	 *            the name of the service
	 * @return a randomly choosen node
	 */
	public String getActiveNodeRandom(String service) {
		Cache cache = getNodeSetCache(service);
		if (cache == null)
			return null;
		ClusterNode[] aa = cache.activeArray;
		if (aa == null)
			return null;
		return aa[RandomUtils.nextInt(0, aa.length)].address;
	}

	/**
	 * Build a status of the given service. The list of active nodes and the
	 * list of inactive nodes with their latest status.
	 * 
	 * @param service
	 *            the name of the service
	 * @return the status of the service
	 */
	public ClusterServiceStatusJson getServiceStatus(String service) {
		Cache cache = getNodeSetCache(service);
		if (cache == null)
			return new ClusterServiceStatusJson();
		List<String> activeList = buildList(cache.activeArray);
		if (cache.inactiveArray == null)
			return new ClusterServiceStatusJson(activeList,
					ClusterServiceStatusJson.EMPTY_MAP);
		Map<String, ClusterNodeStatusJson> inactiveMap = new LinkedHashMap<String, ClusterNodeStatusJson>();
		for (ClusterNode node : cache.inactiveArray)
			inactiveMap.put(node.address, node.getStatus());
		return new ClusterServiceStatusJson(activeList, inactiveMap);
	}

	public void registerMe(String... services) throws URISyntaxException {
		if (clusterMasterSet == null || services == null
				|| services.length == 0)
			return;
		for (String master : clusterMasterSet) {
			logger.info("Registering as a service to " + master);
			try {
				new ClusterClient(master, 60000)
						.register(new ClusterNodeRegisterJson(myAddress,
								services));
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}
		if (clusterNodeShutdownThread == null) {
			clusterNodeShutdownThread = new Thread() {
				@Override
				public void run() {
					try {
						unregisterMe();
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}
			};
			Runtime.getRuntime().addShutdownHook(clusterNodeShutdownThread);
		}
	}

	public void unregisterMe() throws URISyntaxException {
		if (clusterMasterSet == null)
			return;
		for (String master : clusterMasterSet) {
			logger.info("Unregistering to " + master);
			new ClusterClient(master, 60000).unregister(myAddress);
		}
	}

	public TreeMap<String, StatusEnum> getServicesStatus() {
		HashMap<String, ClusterNodeSet> servicesMap = clusterNodeMap
				.getServicesMap();
		if (servicesMap == null)
			return null;
		TreeMap<String, StatusEnum> servicesStatusMap = new TreeMap<String, StatusEnum>();
		for (Map.Entry<String, ClusterNodeSet> entry : servicesMap.entrySet()) {
			Cache cache = entry.getValue().getCache();
			StatusEnum status = ClusterServiceStatusJson.findStatus(
					cache.activeArray.length, cache.inactiveArray.length);
			servicesStatusMap.put(entry.getKey(), status);
		}
		return servicesStatusMap;
	}
}
