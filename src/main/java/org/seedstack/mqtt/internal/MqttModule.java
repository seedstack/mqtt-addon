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

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * Module to initialize {@link MqttClient} and {@link MqttCallback}.
 * 
 * @author thierry.bouvet@mpsa.com
 *
 */
class MqttModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttModule.class);

    private ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions;
    private ConcurrentHashMap<String, IMqttClient> mqttClients;

    public MqttModule(ConcurrentHashMap<String, IMqttClient> mqttClients,
            ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions) {
        this.mqttClientDefinitions = mqttClientDefinitions;
        this.mqttClients = mqttClients;
    }

    @Override
    protected void configure() {
        requestStaticInjection(MqttCallbackAdapter.class);
        for (Entry<String, MqttClientDefinition> entry : mqttClientDefinitions.entrySet()) {
            MqttClientDefinition clientDefinition = entry.getValue();
            String clientName = entry.getKey();
            IMqttClient mqttClient = mqttClients.get(clientName);
            bind(IMqttClient.class).annotatedWith(Names.named(clientName)).toInstance(mqttClient);
            MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
            mqttClient.setCallback(callbackAdapter);
            try {
                MqttClientUtils.connect(mqttClient, clientDefinition);
            } catch (MqttSecurityException e) {
                throw SeedException.wrap(e, MqttErrorCodes.CAN_NOT_CONNECT_MQTT_CLIENT).put("clientName", clientName);
            } catch (MqttException e) {
                throw SeedException.wrap(e, MqttErrorCodes.CAN_NOT_CONNECT_MQTT_CLIENT).put("clientName", clientName);
            }
            MqttPublisherDefinition publisherDefinition = clientDefinition.getPublisherDefinition();
            if (publisherDefinition != null) {
                registerPublisHandler(callbackAdapter, publisherDefinition);
            }
            MqttListenerDefinition listenerDefinition = clientDefinition.getListenerDefinition();
            if (listenerDefinition != null) {
                registerListener(clientName, mqttClient, callbackAdapter, listenerDefinition);
            }
        }

    }

    private void registerPublisHandler(MqttCallbackAdapter callbackAdapter,
            MqttPublisherDefinition publisherDefinition) {
        String className = publisherDefinition.getClassName();
        Class<? extends MqttCallback> clazz = publisherDefinition.getPublisherClass();
        bind(MqttCallback.class).annotatedWith(Names.named(className)).to(clazz);
        callbackAdapter.setPublisherKey(Key.get(MqttCallback.class, Names.named(className)));
    }

    private void registerListener(String clientName, IMqttClient mqttClient, MqttCallbackAdapter callbackAdapter,
            MqttListenerDefinition listenerDefinition) {
        String className = listenerDefinition.getClassName();
        Class<? extends MqttCallback> clazz = listenerDefinition.getListenerClass();
        bind(MqttCallback.class).annotatedWith(Names.named(className)).to(clazz);
        callbackAdapter.setListenerKey(Key.get(MqttCallback.class, Names.named(className)));
        String[] topicFiler = listenerDefinition.getTopicFilter();
        try {
            LOGGER.debug("Subscribe MqttClient [{}] to topicFiler {}", clientName, topicFiler);
            MqttClientUtils.subscribe(mqttClient, listenerDefinition);
        } catch (MqttException e) {
            throw SeedException.wrap(e, MqttErrorCodes.CAN_NOT_CONNECT_SUBSCRIBE).put("clientName", clientName)
                    .put("topic", topicFiler);
        }
    }

}
