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

package com.hazelcast.client.impl.protocol.codec.builtin;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.Bits;
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler;
import com.hazelcast.scheduledexecutor.impl.ScheduledTaskHandlerImpl;

import java.util.ListIterator;

import static com.hazelcast.client.impl.protocol.ClientMessage.BEGIN_FRAME;
import static com.hazelcast.client.impl.protocol.ClientMessage.END_FRAME;
import static com.hazelcast.client.impl.protocol.codec.builtin.CodecUtil.decodeNullable;
import static com.hazelcast.client.impl.protocol.codec.builtin.CodecUtil.encodeNullable;
import static com.hazelcast.client.impl.protocol.codec.builtin.CodecUtil.fastForwardToEndFrame;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.decodeInt;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.encodeInt;

public final class ScheduledTaskHandlerCodec {
    private static final int PARTITION_ID_OFFSET = 0;
    private static final int INITIAL_FRAME_SIZE = PARTITION_ID_OFFSET + Bits.INT_SIZE_IN_BYTES;

    private ScheduledTaskHandlerCodec() {
    }

    public static void encode(ClientMessage clientMessage, ScheduledTaskHandler handler) {
        clientMessage.add(BEGIN_FRAME);

        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[INITIAL_FRAME_SIZE]);
        encodeInt(initialFrame.content, PARTITION_ID_OFFSET, handler.getPartitionId());
        clientMessage.add(initialFrame);

        StringCodec.encode(clientMessage, handler.getSchedulerName());
        StringCodec.encode(clientMessage, handler.getTaskName());
        encodeNullable(clientMessage, handler.getAddress(), AddressCodec::encode);

        clientMessage.add(END_FRAME);
    }

    public static ScheduledTaskHandler decode(ListIterator<ClientMessage.Frame> iterator) {
        // begin frame
        iterator.next();

        ClientMessage.Frame initialFrame = iterator.next();
        int partitionId = decodeInt(initialFrame.content, PARTITION_ID_OFFSET);

        String schedulerName = StringCodec.decode(iterator);
        String taskName = StringCodec.decode(iterator);
        Address address = decodeNullable(iterator, AddressCodec::decode);

        fastForwardToEndFrame(iterator);

        if (address == null) {
            return ScheduledTaskHandlerImpl.of(partitionId, schedulerName, taskName);
        } else {
            return ScheduledTaskHandlerImpl.of(address, schedulerName, taskName);
        }
    }
}
