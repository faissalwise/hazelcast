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

package com.hazelcast.cp.internal.datastructures.unsafe.semaphore.operations;

import com.hazelcast.cp.internal.datastructures.unsafe.semaphore.SemaphoreContainer;
import com.hazelcast.cp.internal.datastructures.unsafe.semaphore.SemaphoreDataSerializerHook;
import com.hazelcast.cp.internal.datastructures.unsafe.semaphore.SemaphoreWaitNotifyKey;
import com.hazelcast.spi.impl.operationservice.Notifier;
import com.hazelcast.spi.impl.operationservice.Operation;
import com.hazelcast.spi.impl.operationservice.WaitNotifyKey;
import com.hazelcast.spi.impl.operationservice.MutatingOperation;

public class ReleaseOperation extends SemaphoreBackupAwareOperation implements Notifier, MutatingOperation {

    public ReleaseOperation() {
    }

    public ReleaseOperation(String name, int permitCount) {
        super(name, permitCount);
    }

    @Override
    public void run() throws Exception {
        SemaphoreContainer semaphoreContainer = getSemaphoreContainer();
        semaphoreContainer.release(getCallerUuid(), permitCount);
        response = true;
    }

    @Override
    public boolean shouldNotify() {
        return permitCount > 0;
    }

    @Override
    public WaitNotifyKey getNotifiedKey() {
        return new SemaphoreWaitNotifyKey(name, "acquire");
    }

    @Override
    public boolean shouldBackup() {
        return permitCount > 0;
    }

    @Override
    public Operation getBackupOperation() {
        return new ReleaseBackupOperation(name, permitCount, getCallerUuid());
    }

    @Override
    public int getClassId() {
        return SemaphoreDataSerializerHook.RELEASE_OPERATION;
    }
}
