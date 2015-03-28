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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class ClusterServicesStatusJson {

	final static List<String> EMPTY_LIST = new ArrayList<String>(0);
	final static Map<String, String> EMPTY_MAP = new HashMap<String, String>();

	public final List<String> active;
	public final Map<String, String> unactive;

	public ClusterServicesStatusJson() {
		this(EMPTY_LIST, EMPTY_MAP);
	}

	public ClusterServicesStatusJson(List<String> active,
			Map<String, String> unactive) {
		this.active = active;
		this.unactive = unactive;
	}

}
