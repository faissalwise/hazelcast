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

package com.hazelcast.map.impl.operation;

import com.hazelcast.map.impl.MapDataSerializerHook;
import com.hazelcast.map.impl.record.Record;
import com.hazelcast.map.impl.record.RecordInfo;
import com.hazelcast.map.impl.record.Records;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.impl.operationservice.BackupOperation;

import java.io.IOException;

public class PutBackupOperation extends KeyBasedMapOperation implements BackupOperation {
    protected boolean putTransient;
    protected RecordInfo recordInfo;

    public PutBackupOperation(String name, Data dataKey, Data dataValue,
                              RecordInfo recordInfo, boolean putTransient,
                              boolean disableWanReplicationEvent) {
        super(name, dataKey, dataValue);
        this.recordInfo = recordInfo;
        this.putTransient = putTransient;
        this.disableWanReplicationEvent = disableWanReplicationEvent;
    }

    public PutBackupOperation() {
    }

    @Override
    protected void runInternal() {
        ttl = recordInfo != null ? recordInfo.getTtl() : ttl;
        maxIdle = recordInfo != null ? recordInfo.getMaxIdle() : maxIdle;
        Record record = recordStore.putBackup(dataKey, dataValue, ttl,
                maxIdle, putTransient, getCallerProvenance());

        if (recordInfo != null) {
            Records.applyRecordInfo(record, recordInfo);
        }
    }

    @Override
    protected void afterRunInternal() {
        if (recordInfo != null) {
            evict(dataKey);
        }
        publishWanUpdate(dataKey, dataValue);

        super.afterRunInternal();
    }

    @Override
    public Object getResponse() {
        return Boolean.TRUE;
    }

    @Override
    public int getClassId() {
        return MapDataSerializerHook.PUT_BACKUP;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        if (recordInfo != null) {
            out.writeBoolean(true);
            recordInfo.writeData(out);
        } else {
            out.writeBoolean(false);
        }
        out.writeBoolean(putTransient);
        out.writeBoolean(disableWanReplicationEvent);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        boolean hasRecordInfo = in.readBoolean();
        if (hasRecordInfo) {
            recordInfo = new RecordInfo();
            recordInfo.readData(in);
        }
        putTransient = in.readBoolean();
        disableWanReplicationEvent = in.readBoolean();
    }
}
