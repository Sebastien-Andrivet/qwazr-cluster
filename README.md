QWAZR Cluster
=============

An open source REST Web Service for Cluster management. It is a component of QWAZR.
https://github.com/qwazr/qwazr.git

oss-cluster is a multi-master daemon in charge of maintaining a cluster of servers and services.
It has been build to be fully redondant and fault tolerant.

The main features are:
- Registration of server nodes and the hosted services.
- Monitoring of the server nodes.
- Synchronisation between oss-cluster nodes.
- Providing a list of the active nodes for one service.

Quickstart
----------

### Requirements

- Check that you have installed a [JAVA Runtime Environment 8](http://openjdk.java.net/install/)
- If you want to compile the software you need [Maven 3.0](http://maven.apache.org/)

### Compilation

Clone the source code:

```shell
git clone https://github.com/qwazr/qwazr-cluster.git
```

Compile and package (the binary will located in the target directory):

```shell
mvn clean package
```

### Download packages

You can download our nightly build available here:
http://download.opensearchserver.com/qwazr-cluster

### Usage

#### Configuration

The configuration file is in YAML format. Just provide the addresses and port of your oss-cluster master servers.

```yaml
masters: ["192.168.0.10:9099","192.168.0.11:9099"]
```

#### Start the server

In the command line, provide the IP address and the port of this instance.

```shell
java -jar target/oss-cluster-xxx-exec.jar -n 192.168.0.10 -p 9099
```

You can run several master servers, they will automatically be synchronised. 

### The REST/JSON API

#### Get an overall status of the cluster

```shell
curl -XGET http://192.168.0.10:9099/cluster
```

```json
{
  "is_master" : true,
  "active_nodes" : [ "http://192.168.0.62:8080" ],
  "inactive_nodes" : {
    "http://192.168.0.65:8080" : {
      "online" : false,
      "state" : "undetermined"
    }
  },
  "services" : {
    "extractor" : "degraded",
    "job" : "ok",
    "renderer" : "failure"
  },
  "masters" : [ "http://192.168.0.10:9099", "http://192.168.0.11:9099" ],
  "last_executions" : {
    "Master sync" : "2015-03-29T00:00:31.594+0000",
    "Nodes monitoring" : "2015-03-29T00:01:41.651+0000"
  }
}
```

#### Register a server node and its services.

This API works like an upsert. The first call create the resource. The next call update the resource.

```shell
curl -H "Content-Type: application/json" \
	-d '{"address": "http://192.168.0.65:8080","services": ["job","extractor"]}' \
	http://192.168.0.10:9099
```
#### Unregister a server node.

```shell
curl -XDELETE http://192.168.0.62:9099/cluster?address=http%3A%2F%2F192.168.0.65%3A8080
```

#### Get the list of server nodes for one service

```shell
curl -XGET http://192.168.0.10:9099/cluster/service/{service_name}/active
```

The list is returned in JSON format.

Replace **service_name** by the name of the service.

#### Get one server node  for the given service

```shell
curl -XGET http://192.168.0.10:9099/cluster/service/{service_name}/active/random
```

The address is returned in TEXT/PLAIN format.

Issues and change Log
---------------------

Issues and milestones are tracked on GitHub:

- [Open issues](https://github.com/qwazr/qwazr-cluster/issues?q=is%3Aopen+is%3Aissue)
- [Closed issues](https://github.com/qwazr/qwazr-cluster/issues?q=is%3Aissue+is%3Aclosed)

License
-------

Copyright 2014-2015 [OpenSearchServer Inc.](http://www.opensearchserver.com)


Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.