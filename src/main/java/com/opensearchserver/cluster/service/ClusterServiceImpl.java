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

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.opensearchserver.cluster.manager.ClusterManager;
import com.opensearchserver.cluster.manager.ClusterNode;
import com.opensearchserver.utils.json.JsonApplicationException;

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

	@Override
	public ClusterNodeStatusJson register(ClusterNodeRegisterJson register) {
		if (register == null)
			throw new WebApplicationException(Status.NOT_ACCEPTABLE);
		ClusterManager manager = ClusterManager.INSTANCE;
		try {
			ClusterNode clusterNode = manager.setClusterNode(register.address,
					register.services);
			return clusterNode.getStatus();
		} catch (Exception e) {
			throw new JsonApplicationException(e);
		}
	}

	@Override
	public Response unregister(String address) {
		if (address == null)
			throw new WebApplicationException(Status.NOT_ACCEPTABLE);
		ClusterManager manager = ClusterManager.INSTANCE;
		try {
			ClusterNode clusterNode = manager.removeClusterNode(address);
			return clusterNode == null ? Response.status(Status.NOT_FOUND)
					.build() : Response.ok().build();
		} catch (Exception e) {
			throw new JsonApplicationException(e);
		}
	}

	@Override
	public ClusterStatusJson getInactiveNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClusterStatusJson getActiveNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNodes(String service_name, Boolean random) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClusterStatusJson getInactiveNodes(String service_name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClusterStatusJson getActiveNodes(String service_name) {
		// TODO Auto-generated method stub
		return null;
	}
}
