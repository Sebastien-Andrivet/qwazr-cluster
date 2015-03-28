/**
 * Copyright 2014-2015 OpenSearchServer Inc.
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
package com.opensearchserver.cluster.test;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.opensearchserver.cluster.ClusterClient;
import com.opensearchserver.cluster.ClusterServer;
import com.opensearchserver.cluster.service.ClusterNodeRegisterJson;
import com.opensearchserver.cluster.service.ClusterNodeStatusJson;
import com.opensearchserver.cluster.service.ClusterStatusJson;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AllTest {

	private final String CLIENT_ADDRESS = "http://"
			+ ClusterServer.DEFAULT_HOSTNAME + ':' + ClusterServer.DEFAULT_PORT;

	private final int CLIENT_TIMEOUT = 60000;

	private final String[] SERVICES = { "job", "search" };

	static final Logger logger = Logger.getLogger(AllTest.class.getName());

	private ClusterClient getClusterClient() throws URISyntaxException {
		return new ClusterClient(CLIENT_ADDRESS, CLIENT_TIMEOUT);
	}

	@Test
	public void test01_list() throws URISyntaxException {
		ClusterStatusJson result = getClusterClient().list();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.is_master);
		Assert.assertNotNull(result.masters);
		Assert.assertTrue(result.masters.contains(CLIENT_ADDRESS));
	}

	@Test
	public void test10_register_two_services() throws URISyntaxException {
		HashSet<String> serviceSet = new HashSet<String>(
				Arrays.asList(SERVICES));
		ClusterNodeStatusJson result = getClusterClient().register(
				new ClusterNodeRegisterJson(CLIENT_ADDRESS, serviceSet));
		Assert.assertNotNull(result);
		Assert.assertNull(result.error, result.error);
	}

	@Test
	public void test11_check_register() throws URISyntaxException {
		HashSet<String> serviceSet = new HashSet<String>(
				Arrays.asList(SERVICES));
		ClusterNodeStatusJson result = getClusterClient().register(
				new ClusterNodeRegisterJson(CLIENT_ADDRESS, serviceSet));
		Assert.assertNotNull(result);
		Assert.assertNull(result.error, result.error);
	}
}
