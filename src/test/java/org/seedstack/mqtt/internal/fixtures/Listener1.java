/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.internal.fixtures;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MqttListener;

@MqttListener(clientName = "clientOK1", topicFilter = "t1", qos = "0")
public class Listener1 implements MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {
        // Just for test
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // Just for test
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Just for test
    }

}
