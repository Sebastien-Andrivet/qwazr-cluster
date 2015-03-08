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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensearchserver.cluster.json.ClusterNodeStatusJson;
import com.opensearchserver.cluster.json.ClusterNodeStatusJson.State;

public class ClusterNode implements FutureCallback<HttpResponse> {

	private static final Logger logger = LoggerFactory
			.getLogger(ClusterNode.class);

	public final String name;

	public final List<String> services;

	final URI baseURI;

	final URI checkURI;

	private volatile long latencyStart;

	private volatile String checkToken;

	private volatile ClusterNodeStatusJson clusterNodeStatus;

	/**
	 * Should never fail. The class will take care of the status of the cluster
	 * node.
	 * 
	 * @param hostname
	 *            The hostname of the node
	 * @param services
	 *            The list of services provided by this node
	 * @throws URISyntaxException
	 */
	ClusterNode(String hostname, List<String> services)
			throws URISyntaxException {
		this.baseURI = toUri(hostname, null);
		this.name = baseURI.toString().intern();
		this.services = services;
		checkURI = new URI(baseURI.getScheme(), null, baseURI.getHost(),
				baseURI.getPort(), "/cluster", null, null);
		latencyStart = 0;
		checkToken = null;
		setStatus(0, State.undetermined, null, null);
	}

	/**
	 * @return the status
	 */
	public ClusterNodeStatusJson getStatus() {
		return clusterNodeStatus;
	}

	private void setStatus(long time, State state, Long latency, String error) {
		this.clusterNodeStatus = new ClusterNodeStatusJson(new Date(time),
				state, latency, error);
		if (error != null)
			logger.warn(error);

	}

	void startCheck(CloseableHttpAsyncClient httpclient) {
		checkToken = UUID.randomUUID().toString();
		HttpHead httpHead = new HttpHead(checkURI);
		httpHead.setHeader(ClusterServiceInterface.HEADER_CHECK_NAME,
				checkToken);
		latencyStart = System.currentTimeMillis();
		httpclient.execute(httpHead, this);
	}

	@Override
	public void completed(HttpResponse response) {
		long time = System.currentTimeMillis();
		long latency = time - latencyStart;
		int responseCode = response.getStatusLine().getStatusCode();
		if (responseCode != 200) {
			setStatus(
					time,
					State.unexpected_response,
					latency,
					"Unexpected response: " + responseCode + " - "
							+ checkURI.toString());
			return;
		}
		Header header = response
				.getFirstHeader(ClusterServiceInterface.HEADER_CHECK_NAME);
		if (header == null) {
			setStatus(
					time,
					State.unexpected_response,
					latency,
					"Unexpected response: " + responseCode + " - "
							+ checkURI.toString());
			return;
		}
		if (!checkToken.equals(header.getValue())) {
			setStatus(
					time,
					State.unexpected_response,
					latency,
					"Unexpected response: " + responseCode + " - "
							+ checkURI.toString());
			return;
		}
		setStatus(time, State.online, latency, null);
	}

	@Override
	public void failed(Exception ex) {
		long time = System.currentTimeMillis();
		long latency = time - latencyStart;
		setStatus(time, State.unreachable, latency, "Cluster node failure  - "
				+ checkURI.toString());
	}

	@Override
	public void cancelled() {
		logger.warn("Cluster node cancelled " + checkURI.toString());
	}

	public static URI toUri(String hostname, Integer port)
			throws URISyntaxException {
		if (!hostname.contains("//"))
			hostname = "//" + hostname;
		URI u = new URI(hostname);
		if (port == null)
			port = u.getPort();
		if (port == -1)
			port = ClusterManager.INSTANCE.port;
		return new URI(StringUtils.isEmpty(u.getScheme()) ? "http"
				: u.getScheme(), null, u.getHost(), port, null, null, null);
	}

}
