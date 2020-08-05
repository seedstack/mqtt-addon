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
package org.seedstack.mqtt.internal.fixtures;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MqttRejectHandler;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;

@MqttRejectHandler(clients = "${test.clientOK1}")
public class MyRejectHandler implements MqttRejectedExecutionHandler {

    @Override
    public void reject(String topic, MqttMessage message) {
        // nothing to do

    }

}
