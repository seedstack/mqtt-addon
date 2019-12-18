/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.mqtt.internal;

import org.eclipse.paho.client.mqttv3.MqttCallback;

/**
 * Defined all topics/qos to listen.
 */
class MqttListenerDefinition {
    private final String[] topicFilter;
    private final String className;
    private final int[] qos;
    private final Class<? extends MqttCallback> listenerClass;

    MqttListenerDefinition(Class<? extends MqttCallback> mqttListenerClass, String className,
                           String[] topicFilter, int[] qos) {
        this.topicFilter = topicFilter;
        this.className = className;
        this.qos = qos;
        this.listenerClass = mqttListenerClass;
    }

    String[] getTopicFilter() {
        return topicFilter;
    }

    String getClassName() {
        return className;
    }

    int[] getQos() {
        return qos;
    }

    Class<? extends MqttCallback> getListenerClass() {
        return listenerClass;
    }
}
