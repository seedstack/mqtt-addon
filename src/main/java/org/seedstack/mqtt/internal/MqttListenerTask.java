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

import java.util.concurrent.ThreadPoolExecutor;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.seed.SeedException;

/**
 * Listener Task running in a {@link ThreadPoolExecutor}. This task is used when
 * a {@link MqttMessage} is arrived.
 * 
 * @author thierry.bouvet@mpsa.com
 *
 */
class MqttListenerTask implements Runnable {
    private MqttCallback listener;
    private String topic;
    private MqttMessage message;

    /**
     * Default constructor
     * 
     * @param listener
     *            real listener to call
     * @param topic
     *            topic received
     * @param message
     *            {@link MqttMessage} received
     */
    public MqttListenerTask(MqttCallback listener, String topic, MqttMessage message) {
        this.listener = listener;
        this.topic = topic;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            listener.messageArrived(topic, message);
        } catch (Exception e) {
            throw SeedException.wrap(e, MqttErrorCodes.LISTENER_ERROR);
        }
    }

}
