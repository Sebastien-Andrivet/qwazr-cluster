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

import java.util.LinkedHashSet;

import org.apache.commons.lang3.RandomUtils;

import com.opensearchserver.utils.LockUtils.ReadWriteLock;

public class ClusterNodeSet {

	private final ReadWriteLock readWriteLock = new ReadWriteLock();

	private volatile ClusterNode[] activeCacheArray;
	private final LinkedHashSet<ClusterNode> activeSet;
	private final LinkedHashSet<ClusterNode> inactiveSet;

	ClusterNodeSet() {
		activeCacheArray = null;
		activeSet = new LinkedHashSet<ClusterNode>();
		inactiveSet = new LinkedHashSet<ClusterNode>();
	}

	private void buildActiveCacheArray() {
		activeCacheArray = activeSet.toArray(new ClusterNode[activeSet.size()]);
	}

	/**
	 * Move the node to the active set
	 * 
	 * @param node
	 *            The cluster not to insert
	 */
	void active(ClusterNode node) {
		// We check first if it is not already present in the right list
		readWriteLock.r.lock();
		try {
			if (activeSet.contains(node))
				return;
		} finally {
			readWriteLock.r.unlock();
		}
		readWriteLock.w.lock();
		try {
			inactiveSet.remove(node);
			activeSet.add(node);
			buildActiveCacheArray();
		} finally {
			readWriteLock.w.unlock();
		}
	}

	/**
	 * Move the node to the inactive set
	 * 
	 * @param node
	 *            The cluster not to insert
	 */
	void inactive(ClusterNode node) {
		// We check first if it is not already present in the right list
		readWriteLock.r.lock();
		try {
			if (inactiveSet.contains(node))
				return;
		} finally {
			readWriteLock.r.unlock();
		}
		readWriteLock.w.lock();
		try {
			activeSet.remove(node);
			inactiveSet.add(node);
			buildActiveCacheArray();
		} finally {
			readWriteLock.w.unlock();
		}
	}

	/**
	 * @param node
	 *            The clusterNode to insert
	 */
	void insert(ClusterNode node) {
		if (node.isActive())
			active(node);
		else
			inactive(node);
	}

	/**
	 * 
	 * @param node
	 *            The ClusterNode to remove
	 */
	void remove(ClusterNode node) {
		readWriteLock.w.lock();
		try {
			activeSet.remove(node);
			inactiveSet.remove(node);
			buildActiveCacheArray();
		} finally {
			readWriteLock.w.unlock();
		}
	}

	/**
	 * @return a clusterNode choose randomly
	 */
	ClusterNode getRandom() {
		ClusterNode[] aa = activeCacheArray;
		if (aa == null)
			return null;
		return aa[RandomUtils.nextInt(0, aa.length)];
	}

	/**
	 * @return if the set is empty
	 */
	boolean isEmpty() {
		readWriteLock.r.lock();
		try {
			return activeSet.isEmpty() && inactiveSet.isEmpty();
		} finally {
			readWriteLock.r.unlock();
		}
	}

}
