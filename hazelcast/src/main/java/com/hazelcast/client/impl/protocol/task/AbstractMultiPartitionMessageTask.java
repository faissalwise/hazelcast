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

package com.hazelcast.client.impl.protocol.task;

import com.hazelcast.client.impl.operations.OperationFactoryWrapper;
import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.nio.Connection;
import com.hazelcast.spi.impl.operationservice.OperationFactory;
import com.hazelcast.spi.impl.operationservice.impl.OperationServiceImpl;
import com.hazelcast.util.collection.PartitionIdSet;

import java.util.Map;

public abstract class AbstractMultiPartitionMessageTask<P> extends AbstractMessageTask<P>
        implements ExecutionCallback<Map<Integer, Object>> {

    protected AbstractMultiPartitionMessageTask(ClientMessage clientMessage, Node node, Connection connection) {
        super(clientMessage, node, connection);
    }

    @Override
    protected void processMessage() {
        OperationFactory operationFactory = new OperationFactoryWrapper(createOperationFactory(), endpoint.getUuid());
        OperationServiceImpl operationService = nodeEngine.getOperationService();
        operationService.invokeOnPartitionsAsync(getServiceName(), operationFactory, getPartitions()).andThen(this);
    }

    public abstract PartitionIdSet getPartitions();

    protected abstract OperationFactory createOperationFactory();

    protected abstract Object reduce(Map<Integer, Object> map);

    @Override
    public final void onFailure(Throwable throwable) {
        handleProcessingFailure(throwable);
    }

    @Override
    public final void onResponse(Map<Integer, Object> map) {
        sendResponse(reduce(map));
    }

}
