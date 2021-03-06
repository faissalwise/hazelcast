/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.wan.impl;

import com.hazelcast.config.AbstractWanPublisherConfig;
import com.hazelcast.config.WanReplicationConfig;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.wan.WanReplicationEvent;
import com.hazelcast.wan.WANReplicationQueueFullException;
import com.hazelcast.wan.WanReplicationEndpoint;

public class FullQueueWanReplication implements WanReplicationEndpoint {

    @Override
    public void init(Node node,
                     WanReplicationConfig wanReplicationConfig,
                     AbstractWanPublisherConfig wanPublisherConfig) {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void publishReplicationEvent(WanReplicationEvent event) {
    }

    @Override
    public void publishReplicationEventBackup(WanReplicationEvent event) {
    }

    @Override
    public void republishReplicationEvent(WanReplicationEvent event) {
    }

    @Override
    public void checkWanReplicationQueues() {
        throw new WANReplicationQueueFullException("WAN event queue is full");
    }
}
