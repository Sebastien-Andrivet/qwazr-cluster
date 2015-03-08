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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import com.opensearchserver.utils.server.RestApplication;
import com.opensearchserver.utils.server.RestServer;

public class ClusterServer extends RestServer {

	private final static int DEFAULT_PORT = 9099;
	private final static String DEFAULT_HOSTNAME = "0.0.0.0";
	private final static String MAIN_JAR = "oss-cluster.jar";
	private final static String DEFAULT_DATADIR_NAME = "opensearchserver_cluster";

	private ClusterServer() {
		super(DEFAULT_HOSTNAME, DEFAULT_PORT, MAIN_JAR,
				ClusterApplication.class, DEFAULT_DATADIR_NAME);
	}

	public static class ClusterApplication extends RestApplication {

		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> classes = super.getClasses();
			classes.add(ClusterServiceImpl.class);
			return classes;
		}
	}

	@Override
	public void beforeStart(CommandLine cmd, File data_directory)
			throws IOException, ParseException {
		ClusterManager.load(this, data_directory);
	}

	public static void main(String[] args) throws IOException, ParseException,
			ServletException {
		new ClusterServer().start(args);
	}

}
