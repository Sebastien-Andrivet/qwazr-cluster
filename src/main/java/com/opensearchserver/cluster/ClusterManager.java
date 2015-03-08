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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
			throws IOException, URISyntaxException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		INSTANCE = new ClusterManager(server, directory);
	}

	public static final String CLUSTER_JSON_NAME = "cluster.json";

	private final ConcurrentHashMap<String, ClusterNode> clusterNodeMap;

	private final Set<String> masters;

	private List<ClusterNode> clusterNodeList;

	private final File clusterJsonFile;

	private final ClusterMonitoringThread clusterMonitoringThread;

	private final String meURI;

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
			return;
		}
		clusterNodeMap = new ConcurrentHashMap<String, ClusterNode>();
		if (cluster.nodes != null) {
			for (String nodeHostname : cluster.nodes) {
				ClusterNode clusterNode = new ClusterNode(nodeHostname);
				clusterNodeMap.put(clusterNode.baseURI.toString().intern(),
						clusterNode);
			}
		}
		masters = new HashSet<String>();
		if (cluster.masters != null) {
			for (String masterHostname : cluster.masters)
				masters.add(ClusterNode.toUri(masterHostname, null).toString()
						.intern());
		}
		buildNodeList();
		if (masters.contains(meURI))
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

	List<ClusterNode> getClusterNodeList() {
		return clusterNodeList;
	}

	Set<String> getMasters() {
		return masters;
	}

}
