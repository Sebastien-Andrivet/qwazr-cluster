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
package com.opensearchserver.cluster.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.opensearchserver.cluster.manager.ClusterManager;
import com.opensearchserver.cluster.manager.ClusterNode;

@JsonInclude(Include.NON_EMPTY)
public class ClusterStatusJson {

	public final boolean is_master;
	public final Map<String, ClusterNodeStatusJson> nodes;
	public final Map<String, Set<String>> services;
	public final Set<String> masters;

	public ClusterStatusJson() {
		is_master = false;
		nodes = null;
		services = null;
		masters = null;
	}

	public ClusterStatusJson(ClusterManager clusterManager) {
		this.is_master = clusterManager.isMaster();
		this.nodes = new HashMap<String, ClusterNodeStatusJson>();
		this.services = new HashMap<String, Set<String>>();
		this.masters = clusterManager.getMasterSet();
	}

	public void addNodeStatus(ClusterNode node) {
		nodes.put(node.address, node.getStatus());
		if (node.services == null)
			return;
		for (String service : node.services) {
			service = service.intern();
			Set<String> serviceNodes = services.get(service);
			if (serviceNodes == null) {
				serviceNodes = new HashSet<String>();
				services.put(service, serviceNodes);
			}
			serviceNodes.add(node.address);
		}
	}
}
