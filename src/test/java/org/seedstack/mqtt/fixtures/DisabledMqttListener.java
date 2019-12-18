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

package org.seedstack.mqtt.fixtures;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MqttListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link @MqttListener} fixture used to test Mqtt communication.
 */
@MqttListener(clients = "", topics = {"${test.dest1.name}", "${test.dest2.name}"}, qos = {
        "${test.dest1.qos}", "${test.dest2.qos}"})
public class DisabledMqttListener implements MqttCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisabledMqttListener.class);

    public static String messageReceived = null;

    @Override
    public void connectionLost(Throwable cause) {
        throw new IllegalStateException("should not be reached");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        throw new IllegalStateException("should not be reached");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        throw new IllegalStateException("should not be reached");
    }
}
