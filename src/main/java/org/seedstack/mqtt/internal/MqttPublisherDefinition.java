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

import org.eclipse.paho.client.mqttv3.MqttCallback;

/**
 * Defined topic listener to create.
 */
class MqttPublisherDefinition {
    private final String className;
    private final Class<? extends MqttCallback> listenerClass;

    MqttPublisherDefinition(Class<? extends MqttCallback> mqttListenerClass, String className) {
        this.className = className;
        this.listenerClass = mqttListenerClass;
    }

    String getClassName() {
        return className;
    }

    Class<? extends MqttCallback> getPublisherClass() {
        return listenerClass;
    }
}
