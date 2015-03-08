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
package com.opensearchserver.cluster.json;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class ClusterNodeStatusJson {

	public static enum State {

		/**
		 * The node is online
		 */
		online,

		/**
		 * The node is not reachable
		 */
		unreachable,

		/**
		 * The node gave a unexpected response
		 */
		unexpected_response,

		/**
		 * The status of the node is not yet determined
		 */
		undetermined;
	}

	final public boolean online;

	final public Date latest_check;

	final public State state;

	final public Long latency;

	final public String error;

	public ClusterNodeStatusJson(Date latest_check, State state, Long latency,
			String error) {
		this.latest_check = latest_check;
		this.state = state;
		this.latency = latency;
		this.error = error;
		this.online = state != null && state == State.online;
	}

}