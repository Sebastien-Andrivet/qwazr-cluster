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

import java.util.List;

import javax.ws.rs.core.Response;

import com.opensearchserver.cluster.json.ClusterStatusJson;

public class ClusterServiceImpl implements ClusterServiceInterface {

	@Override
	public ClusterStatusJson list() {
		List<ClusterNode> clusterNodeList = ClusterManager.INSTANCE
				.getClusterNodeList();
		if (clusterNodeList == null)
			return null;
		ClusterStatusJson clusterStatus = new ClusterStatusJson(
				ClusterManager.INSTANCE);
		for (ClusterNode clusterNode : clusterNodeList)
			clusterStatus.addNodeStatus(clusterNode);
		return clusterStatus;
	}

	@Override
	public Response check(String checkValue) {
		return Response.ok()
				.header(ClusterServiceInterface.HEADER_CHECK_NAME, checkValue)
				.build();
	}
}
