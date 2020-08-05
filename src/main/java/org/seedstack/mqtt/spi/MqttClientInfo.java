/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.spi;

/**
 * Information related to an Mqtt client. Can be used for monitoring purpose.
 */
public class MqttClientInfo {

    private String clientId;

    private String[] topicFilters;

    private String mqttReconnectionMode;

    private MqttPoolInfo mqttPoolInfo;

    private int reconnectionInterval;

    private String uri;

    private int keepAliveInterval;

    private boolean cleanSession;

    private int mqttVersion;

    private int connectionTimeout;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String[] getTopicFilters() {
        if (topicFilters != null) {
            return topicFilters.clone();
        } else {
            return new String[]{};
        }
    }

    public void setTopicFilters(String[] topicFilters) {
        this.topicFilters = topicFilters.clone();
    }

    public String getMqttReconnectionMode() {
        return mqttReconnectionMode;
    }

    public void setMqttReconnectionMode(String mqttReconnectionMode) {
        this.mqttReconnectionMode = mqttReconnectionMode;
    }

    public MqttPoolInfo getMqttPoolInfo() {
        return mqttPoolInfo;
    }

    public void setMqttPoolInfo(MqttPoolInfo mqttPoolInfo) {
        this.mqttPoolInfo = mqttPoolInfo;
    }

    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    public void setReconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public int getMqttVersion() {
        return mqttVersion;
    }

    public void setMqttVersion(int mqttVersion) {
        this.mqttVersion = mqttVersion;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
