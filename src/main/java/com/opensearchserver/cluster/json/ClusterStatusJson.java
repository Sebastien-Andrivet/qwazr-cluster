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
package com.opensearchserver.cluster.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.opensearchserver.cluster.ClusterManager;
import com.opensearchserver.cluster.ClusterNode;

@JsonInclude(Include.NON_EMPTY)
public class ClusterStatusJson {

	public final boolean is_master;
	public final Map<String, ClusterNodeStatusJson> nodes;
	public final Map<String, List<String>> masters;

	public ClusterStatusJson(ClusterManager clusterManager) {
		this.is_master = clusterManager.isMaster();
		this.nodes = new HashMap<String, ClusterNodeStatusJson>();
		this.masters = clusterManager.getMasters();
	}

	public void addNodeStatus(ClusterNode node) {
		nodes.put(node.name, node.getStatus());
	}

}
