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

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * {@link MqttCallback} used for default reconnection mode.
 * 
 * @author thierry.bouvet@mpsa.com
 *
 */
class MqttCallbackAdapter implements MqttCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttCallbackAdapter.class);

    @Inject
    private static Injector injector;

    private Key<MqttCallback> listenerKey;
    private Key<MqttCallback> publisherKey;
    private IMqttClient mqttClient;
    private MqttClientDefinition clientDefinition;

    /**
     * Default constructor.
     * 
     * @param mqttClient
     *            mqttClient to reconnect if needed.
     * @param clientDefinition
     *            {@link MqttClientDefinition} for the current mqttClient.
     */
    public MqttCallbackAdapter(IMqttClient mqttClient, MqttClientDefinition clientDefinition) {
        this.mqttClient = mqttClient;
        this.clientDefinition = clientDefinition;
    }

    @Override
    public void connectionLost(Throwable cause) {
        LOGGER.warn("MQTT connection lost for client: {}", mqttClient.getClientId(), cause);
        switch (clientDefinition.getReconnectionMode()) {
        case NONE:
            break;
        case CUSTOM:
            if (publisherKey != null) {
                injector.getInstance(publisherKey).connectionLost(cause);
            }
            if (listenerKey != null) {
                injector.getInstance(listenerKey).connectionLost(cause);
            }
            break;
        case ALWAYS:
        default:
            reconnect();
            break;
        }
    }

    private void reconnect() {
        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                try {
                    LOGGER.debug("Trying to reconnect {}", mqttClient.getClientId());
                    MqttClientUtils.connect(mqttClient, clientDefinition);
                    LOGGER.info("Client {} is now reconnected", mqttClient.getClientId());
                    if (clientDefinition.getListenerDefinition() != null) {
                        MqttClientUtils.subscribe(mqttClient, clientDefinition.getListenerDefinition());
                    }
                    timer.cancel();
                } catch (MqttSecurityException e) {
                    LOGGER.trace("Can not reconnect mqttclient {}", mqttClient.getClientId(), e);
                } catch (MqttException e) {
                    LOGGER.trace("Can not reconnect mqttclient {}", mqttClient.getClientId(), e);
                }

            }
        };
        timer.scheduleAtFixedRate(task, clientDefinition.getReconnectionInterval() * 1000,
                clientDefinition.getReconnectionInterval() * 1000);

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        injector.getInstance(listenerKey).messageArrived(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (publisherKey != null) {
            injector.getInstance(publisherKey).deliveryComplete(token);
        }
    }

    public void setListenerKey(Key<MqttCallback> key) {
        this.listenerKey = key;
    }

    public void setPublisherKey(Key<MqttCallback> publisherKey) {
        this.publisherKey = publisherKey;
    }

}
