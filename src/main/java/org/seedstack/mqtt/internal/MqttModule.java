/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.mqtt.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

class MqttModule extends AbstractModule {
    private final ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions;
    private final ConcurrentHashMap<String, IMqttClient> mqttClients;
    private final ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters;

    MqttModule(ConcurrentHashMap<String, IMqttClient> mqttClients,
               ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions,
               ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters) {
        this.mqttClientDefinitions = mqttClientDefinitions;
        this.mqttClients = mqttClients;
        this.mqttCallbackAdapters = mqttCallbackAdapters;
    }

    @Override
    protected void configure() {
        requestStaticInjection(MqttCallbackAdapter.class);
        for (Entry<String, MqttClientDefinition> entry : mqttClientDefinitions.entrySet()) {
            MqttClientDefinition clientDefinition = entry.getValue();
            String clientName = entry.getKey();
            IMqttClient mqttClient = mqttClients.get(clientName);
            bind(IMqttClient.class).annotatedWith(Names.named(clientName)).toInstance(mqttClient);
            MqttCallbackAdapter callbackAdapter = mqttCallbackAdapters.get(clientName);
            mqttClient.setCallback(callbackAdapter);

            MqttPublisherDefinition publisherDefinition = clientDefinition.getPublisherDefinition();
            if (publisherDefinition != null) {
                registerPublishHandler(callbackAdapter, publisherDefinition);
            }
            MqttListenerDefinition listenerDefinition = clientDefinition.getListenerDefinition();
            if (listenerDefinition != null) {
                registerListener(callbackAdapter, clientDefinition);
            }
        }
    }

    private void registerPublishHandler(MqttCallbackAdapter callbackAdapter, MqttPublisherDefinition publisherDefinition) {
        String className = publisherDefinition.getClassName();
        Class<? extends MqttCallback> clazz = publisherDefinition.getPublisherClass();
        bind(MqttCallback.class).annotatedWith(Names.named(className)).to(clazz);
        callbackAdapter.setPublisherKey(Key.get(MqttCallback.class, Names.named(className)));
    }

    private void registerListener(MqttCallbackAdapter callbackAdapter, MqttClientDefinition clientDefinition) {
        MqttListenerDefinition listenerDefinition = clientDefinition.getListenerDefinition();
        String className = listenerDefinition.getClassName();
        Class<? extends MqttCallback> clazz = listenerDefinition.getListenerClass();
        bind(MqttCallback.class).annotatedWith(Names.named(className)).to(clazz);
        callbackAdapter.setListenerKey(Key.get(MqttCallback.class, Names.named(className)));
        MqttPoolDefinition poolDefinition = clientDefinition.getPoolDefinition();
        callbackAdapter.setPool(poolDefinition.getThreadPoolExecutor());
        if (poolDefinition.getRejectHandlerClass() != null) {
            String rejectName = poolDefinition.getRejectHandlerName();
            Class<? extends MqttRejectedExecutionHandler> rejectClass = poolDefinition.getRejectHandlerClass();
            bind(MqttRejectedExecutionHandler.class).annotatedWith(Names.named(rejectName)).to(rejectClass);
            callbackAdapter.setRejectHandlerKey(Key.get(MqttRejectedExecutionHandler.class, Names.named(rejectName)));
        }
    }
}
