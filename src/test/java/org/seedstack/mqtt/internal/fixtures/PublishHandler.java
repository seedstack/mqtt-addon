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
package org.seedstack.mqtt.internal.fixtures;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MqttPublishHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MqttPublishHandler} fixture to test Mqtt communication.
 */
@MqttPublishHandler(clients = "${test.clientOK1}")
public class PublishHandler implements MqttCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishHandler.class);

    @Override
    public void connectionLost(Throwable cause) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        LOGGER.info("Delivered token {}: result [{}]", token.getTopics(), token.isComplete());
    }

}
