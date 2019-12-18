/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.fixtures;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MqttListener;
import org.seedstack.mqtt.MultiListenerIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

@MqttListener(clients = "${" + MultiListenerIT.CUSTOM_PUBLISHER_CLIENT_LISTS_FILTER + "}", topics = "topic", qos = "1")
public class DummyMqttListener implements MqttCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyMqttListener.class);

    public static CountDownLatch messageReceivedCount = new CountDownLatch(2);

    @Override
    public void connectionLost(Throwable cause) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LOGGER.info("New message received : {} {}", topic, new String(message.getPayload(), Charset.forName("UTF-8")));
        messageReceivedCount.countDown();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
