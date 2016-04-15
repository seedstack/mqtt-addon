/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.mqtt.internal;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * Defined all MQTT configuration: {@link MqttConnectOptions},
 * {@link MqttListenerDefinition}. This definition is used to create
 * {@link MqttClient}.
 * 
 * @author thierry.bouvet@mpsa.com
 *
 */
class MqttClientDefinition {

    private String uri;
    private String clientId;

    private MqttListenerDefinition listenerDefinition;
    private MqttPublisherDefinition publisherDefinition;
    private MqttConnectOptionsDefinition connectOptionsDefinition;
    private MqttReconnectionMode reconnectionMode = MqttReconnectionMode.ALWAYS;
    private MqttPoolDefinition poolDefinition;
    private int reconnectionInterval = 2;

    /**
     * Default constructor.
     * 
     * @param uri
     *            mqtt broker url
     * @param clientId
     *            client id to use.
     */
    public MqttClientDefinition(String uri, String clientId) {
        this.uri = uri;
        this.clientId = clientId;
    }

    public String getUri() {
        return uri;
    }

    public String getClientId() {
        return clientId;
    }

    public MqttListenerDefinition getListenerDefinition() {
        return listenerDefinition;
    }

    public void setListenerDefinition(MqttListenerDefinition listenerDefinition) {
        this.listenerDefinition = listenerDefinition;
    }

    public MqttConnectOptionsDefinition getConnectOptionsDefinition() {
        return connectOptionsDefinition;
    }

    public void setConnectOptionsDefinition(MqttConnectOptionsDefinition connectOptionsDefinition) {
        this.connectOptionsDefinition = connectOptionsDefinition;
    }

    public MqttPublisherDefinition getPublisherDefinition() {
        return publisherDefinition;
    }

    public void setPublisherDefinition(MqttPublisherDefinition publisherDefinition) {
        this.publisherDefinition = publisherDefinition;
    }

    public MqttReconnectionMode getReconnectionMode() {
        return reconnectionMode;
    }

    public void setReconnectionMode(MqttReconnectionMode reconnectionMode) {
        this.reconnectionMode = reconnectionMode;
    }

    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    public void setReconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }

    public MqttPoolDefinition getPoolDefinition() {
        return poolDefinition;
    }

    public void setPoolDefinition(MqttPoolDefinition poolDefinition) {
        this.poolDefinition = poolDefinition;
    }

}
