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
package com.opensearchserver.cluster.client;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensearchserver.cluster.service.ClusterNodeRegisterJson;
import com.opensearchserver.cluster.service.ClusterNodeStatusJson;
import com.opensearchserver.cluster.service.ClusterServiceInterface;
import com.opensearchserver.cluster.service.ClusterServiceStatusJson;
import com.opensearchserver.cluster.service.ClusterStatusJson;
import com.opensearchserver.utils.json.client.JsonClientException;
import com.opensearchserver.utils.json.client.JsonMultiClientAbstract;

public class ClusterMultiClient extends
		JsonMultiClientAbstract<ClusterSingleClient> implements
		ClusterServiceInterface {

	private static final Logger logger = LoggerFactory
			.getLogger(ClusterMultiClient.class);

	public ClusterMultiClient(Collection<String> urls, int msTimeOut)
			throws URISyntaxException {
		super(new ClusterSingleClient[urls.size()], urls, msTimeOut);
	}

	@Override
	protected ClusterSingleClient newClient(String url, int msTimeOut)
			throws URISyntaxException {
		return new ClusterSingleClient(url, msTimeOut);
	}

	@Override
	public ClusterStatusJson list() {
		JsonClientException exception = null;
		for (ClusterSingleClient client : this) {
			try {
				return client.list();
			} catch (JsonClientException e) {
				logger.warn(e.getMessage(), exception = e);
			}
		}
		throw exception;
	}

	@Override
	public Map<String, Set<String>> getNodes() {
		JsonClientException exception = null;
		for (ClusterSingleClient client : this) {
			try {
				return client.getNodes();
			} catch (JsonClientException e) {
				logger.warn(e.getMessage(), exception = e);
			}
		}
		throw exception;
	}

	@Override
	public ClusterNodeStatusJson register(ClusterNodeRegisterJson register) {
		ClusterNodeStatusJson result = null;
		for (ClusterSingleClient client : this) {
			try {
				result = client.register(register);
			} catch (JsonClientException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return result;
	}

	@Override
	public Response unregister(String address) {
		Response response = null;
		for (ClusterSingleClient client : this) {
			try {
				response = client.unregister(address);
			} catch (JsonClientException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return response;
	}

	@Override
	public Response check(String checkValue) {
		return Response.status(Status.NOT_IMPLEMENTED).build();
	}

	@Override
	public ClusterServiceStatusJson getServiceStatus(String service_name) {
		JsonClientException exception = null;
		for (ClusterSingleClient client : this) {
			try {
				return client.getServiceStatus(service_name);
			} catch (JsonClientException e) {
				logger.warn(e.getMessage(), exception = e);
			}
		}
		throw exception;
	}

	@Override
	public List<String> getActiveNodes(String service_name) {
		JsonClientException exception = null;
		for (ClusterSingleClient client : this) {
			try {
				return client.getActiveNodes(service_name);
			} catch (JsonClientException e) {
				logger.warn(e.getMessage(), exception = e);
			}
		}
		throw exception;
	}

	@Override
	public String getActiveNodeRandom(String service_name) {
		JsonClientException exception = null;
		for (ClusterSingleClient client : this) {
			try {
				return client.getActiveNodeRandom(service_name);
			} catch (JsonClientException e) {
				logger.warn(e.getMessage(), exception = e);
			}
		}
		throw exception;
	}

}
