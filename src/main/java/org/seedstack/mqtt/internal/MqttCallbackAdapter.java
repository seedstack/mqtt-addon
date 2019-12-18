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
package org.seedstack.mqtt.internal;

import com.google.inject.Injector;
import com.google.inject.Key;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;
import javax.inject.Inject;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MqttCallback} used for default reconnection mode.
 */
class MqttCallbackAdapter implements MqttCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttCallbackAdapter.class);
    @Inject
    private static Injector injector;
    private final IMqttClient mqttClient;
    private final MqttClientDefinition clientDefinition;
    private Key<MqttCallback> listenerKey;
    private Key<MqttCallback> publisherKey;
    private Key<MqttRejectedExecutionHandler> rejectHandlerKey;
    private ThreadPoolExecutor pool;

    /**
     * Default constructor.
     *
     * @param mqttClient       mqttClient to reconnect if needed.
     * @param clientDefinition {@link MqttClientDefinition} for the current mqttClient.
     */
    MqttCallbackAdapter(IMqttClient mqttClient, MqttClientDefinition clientDefinition) {
        this.mqttClient = mqttClient;
        this.clientDefinition = clientDefinition;
    }

    @Override
    public void connectionLost(Throwable cause) {
        LOGGER.warn("MQTT connection lost for client: {}", mqttClient.getClientId(), cause);
        switch (clientDefinition.getConfig().getReconnectionMode()) {
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
                LOGGER.debug("reconnecting MQTT client {}", mqttClient.getClientId(), cause);
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
                    connect();
                    timer.cancel();
                } catch (MqttException e) {
                    LOGGER.debug("Can not connect MQTT client {}", mqttClient.getClientId(), e);
                }

            }
        };
        timer.scheduleAtFixedRate(
                task,
                clientDefinition.getConfig().getReconnectionInterval() * 1000L,
                clientDefinition.getConfig().getReconnectionInterval() * 1000L
        );
    }

    private void connect() throws MqttException {
        LOGGER.debug("Trying to connect {}", mqttClient.getClientId());
        MqttClientUtils.connect(mqttClient, clientDefinition);
        LOGGER.info("Client {} is now connected", mqttClient.getClientId());
        if (clientDefinition.getListenerDefinition() != null) {
            MqttClientUtils.subscribe(mqttClient, clientDefinition.getListenerDefinition());
        }
    }

    void start() {
        try {
            connect();
        } catch (MqttException e) {
            LOGGER.debug("Can not connect MQTT client {}", mqttClient.getClientId(), e);
            connectionLost(e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (pool != null) {
            try {
                pool.submit(new MqttListenerTask(injector.getInstance(listenerKey), topic, message));
            } catch (Exception e) {
                if (this.rejectHandlerKey == null) {
                    MqttListenerDefinition listenerDefinition = clientDefinition.getListenerDefinition();
                    throw SeedException.wrap(e, MqttErrorCode.LISTENER_ERROR)
                            .put("listenerClass", listenerDefinition != null ? listenerDefinition.getClassName() : "<unknown>");
                }
                injector.getInstance(this.rejectHandlerKey).reject(topic, message);
            }
        } else {
            injector.getInstance(listenerKey).messageArrived(topic, message);
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (publisherKey != null) {
            injector.getInstance(publisherKey).deliveryComplete(token);
        }
    }

    void setListenerKey(Key<MqttCallback> key) {
        this.listenerKey = key;
    }

    void setPublisherKey(Key<MqttCallback> publisherKey) {
        this.publisherKey = publisherKey;
    }

    void setPool(ThreadPoolExecutor pool) {
        this.pool = pool;
    }

    void setRejectHandlerKey(Key<MqttRejectedExecutionHandler> rejectHandlerKey) {
        this.rejectHandlerKey = rejectHandlerKey;
    }
}
