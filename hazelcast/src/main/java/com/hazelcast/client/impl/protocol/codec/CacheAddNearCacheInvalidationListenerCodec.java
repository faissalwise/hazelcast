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

package com.hazelcast.client.impl.protocol.codec;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.builtin.*;

import java.util.ListIterator;

import static com.hazelcast.client.impl.protocol.ClientMessage.*;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.*;
import com.hazelcast.logging.Logger;

/**
 * Adds listener to cache. This listener will be used to listen near cache invalidation events.
 * Eventually consistent client near caches should use this method to add invalidation listeners
 * instead of {@link #addInvalidationListener(String, boolean)}
 */
public final class CacheAddNearCacheInvalidationListenerCodec {
    //hex: 0x151E00
    public static final int REQUEST_MESSAGE_TYPE = 1383936;
    //hex: 0x151E01
    public static final int RESPONSE_MESSAGE_TYPE = 1383937;
    private static final int REQUEST_LOCAL_ONLY_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_INITIAL_FRAME_SIZE = REQUEST_LOCAL_ONLY_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = CORRELATION_ID_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    private static final int EVENT_CACHE_INVALIDATION_PARTITION_UUID_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int EVENT_CACHE_INVALIDATION_SEQUENCE_FIELD_OFFSET = EVENT_CACHE_INVALIDATION_PARTITION_UUID_FIELD_OFFSET + UUID_SIZE_IN_BYTES;
    private static final int EVENT_CACHE_INVALIDATION_INITIAL_FRAME_SIZE = EVENT_CACHE_INVALIDATION_SEQUENCE_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    //hex: 0x151E02
    private static final int EVENT_CACHE_INVALIDATION_MESSAGE_TYPE = 1383938;
    private static final int EVENT_CACHE_BATCH_INVALIDATION_INITIAL_FRAME_SIZE = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    //hex: 0x151E03
    private static final int EVENT_CACHE_BATCH_INVALIDATION_MESSAGE_TYPE = 1383939;

    private CacheAddNearCacheInvalidationListenerCodec() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class RequestParameters {

        /**
         * Name of the cache.
         */
        public java.lang.String name;

        /**
         * if true fires events that originated from this node only, otherwise fires all events
         */
        public boolean localOnly;
    }

    public static ClientMessage encodeRequest(java.lang.String name, boolean localOnly) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(false);
        clientMessage.setAcquiresResource(false);
        clientMessage.setOperationName("Cache.AddNearCacheInvalidationListener");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeBoolean(initialFrame.content, REQUEST_LOCAL_ONLY_FIELD_OFFSET, localOnly);
        clientMessage.add(initialFrame);
        StringCodec.encode(clientMessage, name);
        return clientMessage;
    }

    public static CacheAddNearCacheInvalidationListenerCodec.RequestParameters decodeRequest(ClientMessage clientMessage) {
        ListIterator<ClientMessage.Frame> iterator = clientMessage.listIterator();
        RequestParameters request = new RequestParameters();
        ClientMessage.Frame initialFrame = iterator.next();
        request.localOnly = decodeBoolean(initialFrame.content, REQUEST_LOCAL_ONLY_FIELD_OFFSET);
        request.name = StringCodec.decode(iterator);
        return request;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class ResponseParameters {

        /**
         * Registration id for the registered listener.
         */
        public java.lang.String response;
    }

    public static ClientMessage encodeResponse(java.lang.String response) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        clientMessage.add(initialFrame);

        StringCodec.encode(clientMessage, response);
        return clientMessage;
    }

    public static CacheAddNearCacheInvalidationListenerCodec.ResponseParameters decodeResponse(ClientMessage clientMessage) {
        ListIterator<ClientMessage.Frame> iterator = clientMessage.listIterator();
        ResponseParameters response = new ResponseParameters();
        //empty initial frame
        iterator.next();
        response.response = StringCodec.decode(iterator);
        return response;
    }

    public static ClientMessage encodeCacheInvalidationEvent(java.lang.String name, com.hazelcast.nio.serialization.Data key, java.lang.String sourceUuid, java.util.UUID partitionUuid, long sequence) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[EVENT_CACHE_INVALIDATION_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        initialFrame.flags |= ClientMessage.IS_EVENT_FLAG;
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, EVENT_CACHE_INVALIDATION_MESSAGE_TYPE);
        encodeUUID(initialFrame.content, EVENT_CACHE_INVALIDATION_PARTITION_UUID_FIELD_OFFSET, partitionUuid);
        encodeLong(initialFrame.content, EVENT_CACHE_INVALIDATION_SEQUENCE_FIELD_OFFSET, sequence);
        clientMessage.add(initialFrame);
        StringCodec.encode(clientMessage, name);
        CodecUtil.encodeNullable(clientMessage, key, DataCodec::encode);
        CodecUtil.encodeNullable(clientMessage, sourceUuid, StringCodec::encode);
        return clientMessage;
    }
    public static ClientMessage encodeCacheBatchInvalidationEvent(java.lang.String name, java.util.Collection<com.hazelcast.nio.serialization.Data> keys, java.util.Collection<java.lang.String> sourceUuids, java.util.Collection<java.util.UUID> partitionUuids, java.util.Collection<java.lang.Long> sequences) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[EVENT_CACHE_BATCH_INVALIDATION_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        initialFrame.flags |= ClientMessage.IS_EVENT_FLAG;
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, EVENT_CACHE_BATCH_INVALIDATION_MESSAGE_TYPE);
        clientMessage.add(initialFrame);
        StringCodec.encode(clientMessage, name);
        ListMultiFrameCodec.encode(clientMessage, keys, DataCodec::encode);
        ListMultiFrameCodec.encodeNullable(clientMessage, sourceUuids, StringCodec::encode);
        ListUUIDCodec.encode(clientMessage, partitionUuids);
        ListLongCodec.encode(clientMessage, sequences);
        return clientMessage;
    }

    public abstract static class AbstractEventHandler {

        public void handle(ClientMessage clientMessage) {
            int messageType = clientMessage.getMessageType();
            ListIterator<ClientMessage.Frame> iterator = clientMessage.listIterator();
            if (messageType == EVENT_CACHE_INVALIDATION_MESSAGE_TYPE) {
                ClientMessage.Frame initialFrame = iterator.next();
                java.util.UUID partitionUuid = decodeUUID(initialFrame.content, EVENT_CACHE_INVALIDATION_PARTITION_UUID_FIELD_OFFSET);
                long sequence = decodeLong(initialFrame.content, EVENT_CACHE_INVALIDATION_SEQUENCE_FIELD_OFFSET);
                java.lang.String name = StringCodec.decode(iterator);
                com.hazelcast.nio.serialization.Data key = CodecUtil.decodeNullable(iterator, DataCodec::decode);
                java.lang.String sourceUuid = CodecUtil.decodeNullable(iterator, StringCodec::decode);
                handleCacheInvalidationEvent(name, key, sourceUuid, partitionUuid, sequence);
                return;
            }
            if (messageType == EVENT_CACHE_BATCH_INVALIDATION_MESSAGE_TYPE) {
                //empty initial frame
                iterator.next();
                java.lang.String name = StringCodec.decode(iterator);
                java.util.List<com.hazelcast.nio.serialization.Data> keys = ListMultiFrameCodec.decode(iterator, DataCodec::decode);
                java.util.List<java.lang.String> sourceUuids = ListMultiFrameCodec.decodeNullable(iterator, StringCodec::decode);
                java.util.List<java.util.UUID> partitionUuids = ListUUIDCodec.decode(iterator);
                java.util.List<java.lang.Long> sequences = ListLongCodec.decode(iterator);
                handleCacheBatchInvalidationEvent(name, keys, sourceUuids, partitionUuids, sequences);
                return;
            }
            Logger.getLogger(super.getClass()).finest("Unknown message type received on event handler :" + messageType);
        }
        public abstract void handleCacheInvalidationEvent(java.lang.String name, com.hazelcast.nio.serialization.Data key, java.lang.String sourceUuid, java.util.UUID partitionUuid, long sequence);
        public abstract void handleCacheBatchInvalidationEvent(java.lang.String name, java.util.Collection<com.hazelcast.nio.serialization.Data> keys, java.util.Collection<java.lang.String> sourceUuids, java.util.Collection<java.util.UUID> partitionUuids, java.util.Collection<java.lang.Long> sequences);
    }
}
