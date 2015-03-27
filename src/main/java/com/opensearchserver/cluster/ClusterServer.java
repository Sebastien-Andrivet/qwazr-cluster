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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import com.opensearchserver.cluster.manager.ClusterManager;
import com.opensearchserver.cluster.service.ClusterServiceImpl;
import com.opensearchserver.utils.server.AbstractServer;
import com.opensearchserver.utils.server.RestApplication;
import com.opensearchserver.utils.server.ServletApplication;

public class ClusterServer extends AbstractServer {

	private final static int DEFAULT_PORT = 9099;
	private final static String DEFAULT_HOSTNAME = "0.0.0.0";
	private final static String MAIN_JAR = "oss-cluster.jar";
	private final static String DEFAULT_DATADIR_NAME = "opensearchserver_cluster";

	private ClusterServer() {
		super(DEFAULT_HOSTNAME, DEFAULT_PORT, MAIN_JAR, DEFAULT_DATADIR_NAME);
	}

	@ApplicationPath("/")
	public static class ClusterApplication extends RestApplication {

		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> classes = super.getClasses();
			classes.add(ClusterServiceImpl.class);
			return classes;
		}
	}

	public static void load(AbstractServer server, File data_directory,
			Set<Class<?>> classes) throws IOException {
		ClusterManager.load(server, data_directory);
		if (classes != null)
			classes.add(ClusterApplication.class);
	}

	@Override
	public void commandLine(CommandLine cmd) throws IOException {
	}

	@Override
	public void load() throws IOException {
		load(this, getCurrentDataDir(), null);
	}

	@Override
	public RestApplication getRestApplication() {
		return new ClusterApplication();
	}

	@Override
	protected ServletApplication getServletApplication() {
		return null;
	}

	public static void main(String[] args) throws IOException, ParseException,
			ServletException {
		new ClusterServer().start(args);
	}

}
