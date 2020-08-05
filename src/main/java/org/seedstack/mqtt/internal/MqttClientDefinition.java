/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.mqtt.internal;

import org.seedstack.mqtt.MqttConfig;

class MqttClientDefinition {
    private final MqttConfig.ClientConfig clientConfig;
    private final MqttPoolDefinition poolDefinition;
    private MqttListenerDefinition listenerDefinition;
    private MqttPublisherDefinition publisherDefinition;

    MqttClientDefinition(MqttConfig.ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.poolDefinition = new MqttPoolDefinition(clientConfig.getPoolConfig());
    }

    MqttConfig.ClientConfig getConfig() {
        return clientConfig;
    }

    MqttListenerDefinition getListenerDefinition() {
        return listenerDefinition;
    }

    void setListenerDefinition(MqttListenerDefinition listenerDefinition) {
        this.listenerDefinition = listenerDefinition;
    }

    MqttPublisherDefinition getPublisherDefinition() {
        return publisherDefinition;
    }

    void setPublisherDefinition(MqttPublisherDefinition publisherDefinition) {
        this.publisherDefinition = publisherDefinition;
    }

    MqttPoolDefinition getPoolDefinition() {
        return poolDefinition;
    }
}
