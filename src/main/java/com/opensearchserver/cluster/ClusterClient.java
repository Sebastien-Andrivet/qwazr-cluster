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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import com.opensearchserver.cluster.service.ClusterNodeRegisterJson;
import com.opensearchserver.cluster.service.ClusterNodeStatusJson;
import com.opensearchserver.cluster.service.ClusterServiceInterface;
import com.opensearchserver.cluster.service.ClusterServiceStatusJson;
import com.opensearchserver.cluster.service.ClusterStatusJson;
import com.opensearchserver.utils.HttpUtils;
import com.opensearchserver.utils.StringUtils;
import com.opensearchserver.utils.json.JsonClientAbstract;
import com.opensearchserver.utils.json.JsonClientException;

public class ClusterClient extends JsonClientAbstract implements
		ClusterServiceInterface {

	public ClusterClient(String url, int msTimeOut) throws URISyntaxException {
		super(url, msTimeOut);
	}

	@Override
	public ClusterStatusJson list() {
		try {
			URIBuilder uriBuilder = getBaseUrl("/cluster");
			Request request = Request.Get(uriBuilder.build());
			return execute(request, null, msTimeOut, ClusterStatusJson.class,
					200);
		} catch (URISyntaxException | IOException e) {
			throw new JsonClientException(e);
		}
	}

	@Override
	public ClusterNodeStatusJson register(ClusterNodeRegisterJson register) {
		try {
			URIBuilder uriBuilder = getBaseUrl("/cluster");
			Request request = Request.Post(uriBuilder.build());
			return execute(request, register, msTimeOut,
					ClusterNodeStatusJson.class, 200);
		} catch (URISyntaxException | IOException e) {
			throw new JsonClientException(e);
		}
	}

	@Override
	public Response unregister(String address) {
		try {
			URIBuilder uriBuilder = getBaseUrl("/cluster");
			uriBuilder.setParameter("address", address);
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, msTimeOut);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode())
					.build();
		} catch (URISyntaxException | IOException e) {
			throw new JsonClientException(e);
		}
	}

	@Override
	public Response check(String checkValue) {
		return Response.status(Status.NOT_IMPLEMENTED).build();
	}

	@Override
	public ClusterServiceStatusJson getServiceStatus(String service_name) {
		try {
			URIBuilder uriBuilder = getBaseUrl("/cluster/services/"
					+ service_name);
			Request request = Request.Get(uriBuilder.build());
			return execute(request, null, msTimeOut,
					ClusterServiceStatusJson.class, 200);
		} catch (URISyntaxException | IOException e) {
			throw new JsonClientException(e);
		}
	}

	@Override
	public List<String> getActiveNodes(String service_name) {
		try {
			URIBuilder uriBuilder = getBaseUrl("/cluster/services/active"
					+ service_name);
			Request request = Request.Get(uriBuilder.build());
			HttpResponse response = execute(request, null, msTimeOut);
			HttpUtils.checkStatusCodes(response, 200);
			String list = HttpUtils.checkIsEntity(response,
					ContentType.TEXT_PLAIN).toString();
			return Arrays.asList(StringUtils.splitLines(list));
		} catch (URISyntaxException | IOException e) {
			throw new JsonClientException(e);
		}
	}

	@Override
	public String getActiveNodeRandom(String service_name) {
		try {
			URIBuilder uriBuilder = getBaseUrl("/cluster/services/active/random"
					+ service_name);
			Request request = Request.Get(uriBuilder.build());
			HttpResponse response = execute(request, null, msTimeOut);
			HttpUtils.checkStatusCodes(response, 200);
			return HttpUtils.checkIsEntity(response, ContentType.TEXT_PLAIN)
					.toString();
		} catch (URISyntaxException | IOException e) {
			throw new JsonClientException(e);
		}
	}
}
