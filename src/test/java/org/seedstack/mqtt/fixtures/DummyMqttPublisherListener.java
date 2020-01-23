/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.mqtt.fixtures;

import java.util.concurrent.CountDownLatch;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MqttPublishHandler;
import org.seedstack.mqtt.MultiListenerIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MqttPublishHandler(clients = "${" + MultiListenerIT.CUSTOM_PUBLISHER_CLIENT_LISTS_FILTER + "}")
public class DummyMqttPublisherListener implements MqttCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyMqttPublisherListener.class);

    public static CountDownLatch messagePublishedCount = new CountDownLatch(2);

    @Override
    public void connectionLost(Throwable cause) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        LOGGER.info("message delivered to client id : {}", token.getClient().getClientId());
        messagePublishedCount.countDown();
    }
}
