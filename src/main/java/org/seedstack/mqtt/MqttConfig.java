/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.mqtt.internal.MqttConnectOptions;
import org.seedstack.seed.validation.NotBlank;

@Config("mqtt")
public class MqttConfig {
    private boolean enabled = true;
    private Map<String, ClientConfig> clients = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public MqttConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Map<String, ClientConfig> getClients() {
        return Collections.unmodifiableMap(clients);
    }

    public MqttConfig addClient(String name, ClientConfig clientConfig) {
        this.clients.put(name, clientConfig);
        return this;
    }

    public static class ClientConfig {
        @NotBlank
        @SingleValue
        private String serverUri;
        @NotBlank
        private String clientId = MqttClient.generateClientId();
        private ReconnectionMode reconnectionMode = ReconnectionMode.ALWAYS;
        private int reconnectionInterval = 2;
        @Config("connection")
        private MqttConnectOptions connectOptions;
        @Config("pool")
        private PoolConfig poolConfig = new PoolConfig();

        public String getServerUri() {
            return serverUri;
        }

        public ClientConfig setServerUri(String serverUri) {
            this.serverUri = serverUri;
            return this;
        }

        public String getClientId() {
            return clientId;
        }

        public ClientConfig setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ReconnectionMode getReconnectionMode() {
            return reconnectionMode;
        }

        public ClientConfig setReconnectionMode(ReconnectionMode reconnectionMode) {
            this.reconnectionMode = reconnectionMode;
            return this;
        }

        public int getReconnectionInterval() {
            return reconnectionInterval;
        }

        public ClientConfig setReconnectionInterval(int reconnectionInterval) {
            this.reconnectionInterval = reconnectionInterval;
            return this;
        }

        public MqttConnectOptions getConnectOptions() {
            return connectOptions;
        }

        public ClientConfig setConnectOptions(MqttConnectOptions connectOptions) {
            this.connectOptions = connectOptions;
            return this;
        }

        public PoolConfig getPoolConfig() {
            return poolConfig;
        }

        public static class PoolConfig {
            @SingleValue
            private boolean enabled = false;
            private int coreSize = 1;
            private int maxSize = 2;
            private int queueSize = 500;
            private int keepAlive = 60;
            private RejectedExecutionPolicy rejectedExecutionPolicy = RejectedExecutionPolicy.CALLER_RUNS;

            public boolean isEnabled() {
                return enabled;
            }

            public PoolConfig setEnabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }

            public int getCoreSize() {
                return coreSize;
            }

            public PoolConfig setCoreSize(int coreSize) {
                this.coreSize = coreSize;
                return this;
            }

            public int getMaxSize() {
                return maxSize;
            }

            public PoolConfig setMaxSize(int maxSize) {
                this.maxSize = maxSize;
                return this;
            }

            public int getQueueSize() {
                return queueSize;
            }

            public PoolConfig setQueueSize(int queueSize) {
                this.queueSize = queueSize;
                return this;
            }

            public int getKeepAlive() {
                return keepAlive;
            }

            public PoolConfig setKeepAlive(int keepAlive) {
                this.keepAlive = keepAlive;
                return this;
            }

            public RejectedExecutionPolicy getRejectedExecutionPolicy() {
                return rejectedExecutionPolicy;
            }

            public PoolConfig setRejectedExecutionPolicy(RejectedExecutionPolicy rejectedExecutionPolicy) {
                this.rejectedExecutionPolicy = rejectedExecutionPolicy;
                return this;
            }

            /**
             * Supported RejectedExecutionHandler policies :
             * <ul>
             * <li></li>
             * <li>ABORT:  {@link java.util.concurrent.ThreadPoolExecutor.AbortPolicy} is called.</li>
             * <li>DISCARD:  {@link java.util.concurrent.ThreadPoolExecutor.DiscardPolicy} is called.</li>
             * <li>CALLER_RUNS: {@link java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy} is called.</li>
             * <li>DISCARD_OLDEST: {@link java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy} is called.</li>
             * </ul>
             */
            public enum RejectedExecutionPolicy {
                ABORT,
                DISCARD,
                CALLER_RUNS,
                DISCARD_OLDEST
            }
        }

        /**
         * Different reconnection modes when the connection is lost:
         * <ul>
         * <li>NONE: no reconnection.</li>
         * <li>ALWAYS: try to reconnect if connection is lost.</li>
         * <li>CUSTOM: Custom {@link MqttPublishHandler} is call.</li>
         * </ul>
         */
        public enum ReconnectionMode {
            NONE, ALWAYS, CUSTOM
        }
    }
}
