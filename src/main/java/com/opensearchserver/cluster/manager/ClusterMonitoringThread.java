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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import com.opensearchserver.utils.ThreadUtils;

public class ClusterMonitoringThread extends Thread {

	private final int monitoring_period;

	private final RequestConfig requestConfig;
	private final CloseableHttpAsyncClient httpclient;

	ClusterMonitoringThread(int monitoring_period_seconds) {
		super("ClusterMonitoringThread");
		setDaemon(true);
		this.monitoring_period = monitoring_period_seconds * 1000;
		requestConfig = RequestConfig.custom()
				.setSocketTimeout(monitoring_period)
				.setConnectTimeout(monitoring_period).build();
		httpclient = HttpAsyncClients.custom()
				.setDefaultRequestConfig(requestConfig).build();
		httpclient.start();
		start();
	}

	@Override
	public void run() {
		// initial time wait
		ThreadUtils.sleepMs(10000);
		for (;;) {
			long start = System.currentTimeMillis();
			for (ClusterNode clusterNode : ClusterManager.INSTANCE
					.getNodeList())
				clusterNode.startCheck(httpclient);
			long sleep = monitoring_period
					- (System.currentTimeMillis() - start);
			if (sleep > 0)
				ThreadUtils.sleepMs(monitoring_period);
		}
	}
}
