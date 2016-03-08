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
package org.seedstack.mqtt.fixtures;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MqttListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link @MqttListener} fixture used to test Mqtt communication.
 * 
 * @author thierry.bouvet@mpsa.com
 *
 */
@MqttListener(clientName = "client1", topicFilter = { "${test.dest1.name}", "${test.dest2.name}" }, qos = {
        "${test.dest1.qos}", "${test.dest2.qos}" })
public class TestMqttListener implements MqttCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMqttListener.class);

    public static String messageReceived = null;

    @Override
    public void connectionLost(Throwable cause) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        messageReceived = new String(message.getPayload(), StandardCharsets.UTF_8);
        LOGGER.info("New message: {} {}", topic, messageReceived);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

}
