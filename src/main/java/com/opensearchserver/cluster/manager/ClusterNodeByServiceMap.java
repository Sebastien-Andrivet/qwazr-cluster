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

import java.util.HashMap;

public class ClusterNodeByServiceMap {

	private final HashMap<String, ClusterNodeSet> nodesByServiceMap;
	private volatile HashMap<String, ClusterNodeSet> cacheMap;

	ClusterNodeByServiceMap() {
		nodesByServiceMap = new HashMap<String, ClusterNodeSet>();
	}

	private void buildCacheMap() {
		cacheMap = new HashMap<String, ClusterNodeSet>(nodesByServiceMap);
	}

	private ClusterNodeSet getOrNew(String service) {
		service = service.intern();
		ClusterNodeSet nodeSet = nodesByServiceMap.get(service);
		if (nodeSet == null) {
			nodeSet = new ClusterNodeSet();
			nodesByServiceMap.put(service, nodeSet);
		}
		return nodeSet;
	}

	/**
	 * @param clusterNode
	 *            The cluster node to remove
	 */
	void remove(ClusterNode clusterNode) {
		if (clusterNode.services == null)
			return;
		synchronized (nodesByServiceMap) {
			for (String service : clusterNode.services)
				getOrNew(service).remove(clusterNode);
			buildCacheMap();
		}
	}

	/**
	 * @param clusterNode
	 *            The cluster node to insert
	 */
	void insert(ClusterNode clusterNode) {
		if (clusterNode.services == null)
			return;
		synchronized (nodesByServiceMap) {
			for (String service : clusterNode.services)
				getOrNew(service).insert(clusterNode);
			buildCacheMap();
		}
	}

	ClusterNodeSet getClusterNodeSet(String service) {
		return cacheMap.get(service.intern());
	}

}
