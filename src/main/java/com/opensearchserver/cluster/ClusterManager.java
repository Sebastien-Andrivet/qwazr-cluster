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
package com.opensearchserver.cluster;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensearchserver.cluster.json.ClusterJson;
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

	public static final String CLUSTER_JSON_NAME = "cluster.json";

	private final ConcurrentHashMap<String, ClusterNode> clusterNodeMap;

	private final ConcurrentHashMap<String, List<String>> masters;

	private List<ClusterNode> clusterNodeList;

	private final File clusterJsonFile;

	private final ClusterMonitoringThread clusterMonitoringThread;

	private final String meURI;

	private final boolean isMaster;

	protected int port;

	private ClusterManager(AbstractServer server, File rootDirectory)
			throws IOException, URISyntaxException {
		this.port = server.getCurrentTcpPort();
		meURI = ClusterNode.toUri(server.getCurrentHostname(), port).toString()
				.intern();
		logger.info("Server: " + meURI);
		clusterJsonFile = new File(rootDirectory, CLUSTER_JSON_NAME);
		ClusterJson cluster = ClusterJson.newInstance(clusterJsonFile);
		if (cluster == null) {
			clusterNodeMap = null;
			clusterMonitoringThread = null;
			masters = null;
			isMaster = false;
			return;
		}
		clusterNodeMap = new ConcurrentHashMap<String, ClusterNode>();
		if (cluster.nodes != null) {
			for (Map.Entry<String, List<String>> entry : cluster.nodes
					.entrySet()) {
				ClusterNode clusterNode = new ClusterNode(entry.getKey(),
						entry.getValue());
				clusterNodeMap.put(clusterNode.baseURI.toString().intern(),
						clusterNode);
			}
		}
		boolean isMaster = false;
		masters = new ConcurrentHashMap<String, List<String>>();
		if (cluster.masters != null) {
			for (Map.Entry<String, List<String>> entry : cluster.masters
					.entrySet()) {
				String name = ClusterNode.toUri(entry.getKey(), null)
						.toString().intern();
				if (name == meURI)
					isMaster = true;
				for (String service : entry.getValue()) {
					List<String> serviceList = masters.get(service);
					if (serviceList == null) {
						serviceList = new ArrayList<String>(1);
						masters.put(service, serviceList);
					}
					serviceList.add(name);
				}
			}
		}
		buildNodeList();
		this.isMaster = isMaster;
		if (isMaster)
			clusterMonitoringThread = new ClusterMonitoringThread(60);
		else
			clusterMonitoringThread = null;
	}

	private void buildNodeList() {
		List<ClusterNode> newClusterNodeList = new ArrayList<ClusterNode>(
				clusterNodeMap.size());
		for (ClusterNode clusterNode : clusterNodeMap.values())
			newClusterNodeList.add(clusterNode);
		clusterNodeList = newClusterNodeList;
	}

	public List<ClusterNode> getClusterNodeList() {
		return clusterNodeList;
	}

	public Map<String, List<String>> getMasters() {
		return masters;
	}

	public boolean isMaster() {
		return isMaster;
	}

}
