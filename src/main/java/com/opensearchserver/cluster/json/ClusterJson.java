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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.opensearchserver.utils.json.JsonMapper;

@JsonInclude(Include.NON_EMPTY)
public class ClusterJson {

	public final Map<String, List<String>> nodes;
	public final Map<String, List<String>> masters;

	public ClusterJson() {
		nodes = null;
		masters = null;
	}

	public static ClusterJson newInstance(File clusterJsonFile)
			throws IOException {
		if (!clusterJsonFile.exists() || clusterJsonFile.length() == 0)
			return null;
		return JsonMapper.MAPPER.readValue(clusterJsonFile, ClusterJson.class);
	}

}
