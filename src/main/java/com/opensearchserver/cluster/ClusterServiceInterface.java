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

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.opensearchserver.cluster.json.ClusterStatusJson;

@Path("/cluster")
public interface ClusterServiceInterface {

	public final String HEADER_CHECK_NAME = "X-OSS-CLUSTER-CHECK-TOKEN";

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public ClusterStatusJson list();

	@HEAD
	@Path("/")
	public Response check(@HeaderParam(HEADER_CHECK_NAME) String checkValue);

}
